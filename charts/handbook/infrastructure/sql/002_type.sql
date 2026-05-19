-- 002_type.sql : type 도메인 테이블 (types / type_attributes / type_layouts).
-- schema-expert 2026-04-17 DDL 초안 기반. 멱등 CREATE IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS types (
    id                 VARCHAR(255) NOT NULL,
    version            VARCHAR(255) NOT NULL,
    workspace          UUID         NOT NULL,
    effect_date_time   TIMESTAMPTZ  NOT NULL,
    expire_date_time   TIMESTAMPTZ  NOT NULL,
    description        TEXT,
    primitive          BOOLEAN      NOT NULL DEFAULT FALSE,
    parent             VARCHAR(255),
    rev                BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    last_modified_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_modified_by   UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    PRIMARY KEY (id, version, workspace),
    CONSTRAINT fk_types_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT chk_types_period
        CHECK (expire_date_time > effect_date_time)
);

CREATE INDEX IF NOT EXISTS idx_types_workspace_period
    ON types (workspace, effect_date_time, expire_date_time);
CREATE INDEX IF NOT EXISTS idx_types_parent
    ON types (workspace, parent)
    WHERE parent IS NOT NULL;

CREATE TABLE IF NOT EXISTS type_attributes (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    type_id         VARCHAR(255) NOT NULL,
    type_version    VARCHAR(255) NOT NULL,
    workspace       UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    attr_order      SMALLINT     NOT NULL,
    description     TEXT,
    attribute_type  JSONB        NOT NULL,
    nullable        BOOLEAN      NOT NULL DEFAULT FALSE,
    inherited       BOOLEAN      NOT NULL DEFAULT FALSE,
    read_roles      JSONB        NOT NULL DEFAULT '[]'::jsonb,
    write_roles     JSONB        NOT NULL DEFAULT '[]'::jsonb,
    PRIMARY KEY (id),
    CONSTRAINT fk_type_attributes_type
        FOREIGN KEY (type_id, type_version, workspace)
        REFERENCES types(id, version, workspace) ON DELETE CASCADE,
    CONSTRAINT uq_type_attributes_name
        UNIQUE (type_id, type_version, workspace, name)
);

CREATE INDEX IF NOT EXISTS idx_type_attributes_lookup
    ON type_attributes (workspace, type_id, type_version);
CREATE INDEX IF NOT EXISTS idx_type_attributes_type_bulk
    ON type_attributes (workspace, type_id);

CREATE TABLE IF NOT EXISTS type_layouts (
    id                 UUID         NOT NULL,
    workspace          UUID         NOT NULL,
    effect_date_time   TIMESTAMPTZ  NOT NULL,
    expire_date_time   TIMESTAMPTZ  NOT NULL,
    positions          JSONB,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    last_modified_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_modified_by   UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    PRIMARY KEY (id),
    CONSTRAINT fk_type_layouts_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT chk_type_layouts_period
        CHECK (expire_date_time > effect_date_time)
);

CREATE INDEX IF NOT EXISTS idx_type_layouts_workspace
    ON type_layouts (workspace, effect_date_time, expire_date_time);

-- -----------------------------------------------------------------------------
-- Data Integrity Triggers for Types
-- -----------------------------------------------------------------------------

-- 1. 같은 타입(ID) 내에서는 유효기간 중복이 없어야 한다
CREATE OR REPLACE FUNCTION enforce_no_overlap_type_periods()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM types
        WHERE workspace = NEW.workspace
          AND id = NEW.id
          AND version <> NEW.version -- 자기 자신(현재 버전) 제외
          AND (NEW.effect_date_time, NEW.expire_date_time) OVERLAPS (effect_date_time, expire_date_time)
    ) THEN
        RAISE EXCEPTION 'Overlapping periods are not allowed for type id: %, version: %, effect: %, expire: %',
            NEW.id, NEW.version, NEW.effect_date_time, NEW.expire_date_time;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS enforce_no_overlap_type_periods_trigger ON types;
CREATE CONSTRAINT TRIGGER enforce_no_overlap_type_periods_trigger
    AFTER INSERT OR UPDATE ON types
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_no_overlap_type_periods();

-- 2. 부모 타입의 존속성을 확인하고, 해당 기간 동안 gap 없이 NEW를 커버하는지 검사
CREATE OR REPLACE FUNCTION enforce_parent_type_consistency()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (NEW.parent IS NOT NULL AND NEW.parent <> '') THEN
        IF NOT EXISTS (
            SELECT 1
            FROM types
            WHERE workspace = NEW.workspace
              AND id = NEW.parent
              AND (effect_date_time, expire_date_time) OVERLAPS (NEW.effect_date_time, NEW.expire_date_time)
        ) THEN
            RAISE EXCEPTION 'Parent type (id=%) does not exist during the effective period.', NEW.parent;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM (
                     SELECT
                         MIN(effect_date_time) AS combined_start,
                         MAX(expire_date_time) AS combined_end,
                         SUM(CASE WHEN previous_expire_date_time IS NOT NULL AND previous_expire_date_time <> effect_date_time THEN 1 ELSE 0 END) AS gaps
                     FROM (
                              SELECT
                                  effect_date_time,
                                  expire_date_time,
                                  LAG(expire_date_time) OVER (ORDER BY effect_date_time) AS previous_expire_date_time
                              FROM types
                              WHERE workspace = NEW.workspace
                                AND id = NEW.parent
                                AND (effect_date_time, expire_date_time) OVERLAPS (NEW.effect_date_time, NEW.expire_date_time)
                          ) parent_subquery
                 ) merged_period
            WHERE gaps > 0
               OR combined_start > NEW.effect_date_time
               OR combined_end < NEW.expire_date_time
        ) THEN
            RAISE EXCEPTION 'Parent type (id=%) is missing, has gaps, or does not fully cover the period [%, %]',
                NEW.parent, NEW.effect_date_time, NEW.expire_date_time;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS enforce_parent_type_consistency_trigger ON types;
CREATE CONSTRAINT TRIGGER enforce_parent_type_consistency_trigger
    AFTER INSERT OR UPDATE ON types
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_parent_type_consistency();

-- 3. 타입 삭제 시, 상속한 자식 타입 부재 확인
CREATE OR REPLACE FUNCTION prevent_deletion_if_children_exist()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM types AS child
        WHERE child.workspace = OLD.workspace
          AND child.parent = OLD.id
          AND (child.effect_date_time, child.expire_date_time) OVERLAPS (OLD.effect_date_time, OLD.expire_date_time)
    ) THEN
        RAISE EXCEPTION 'Cannot delete parent type (id=%, version=%) as it still has associated children during the period [% → %].',
            OLD.id, OLD.version, OLD.effect_date_time, OLD.expire_date_time;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_deletion_if_children_exist_trigger ON types;
CREATE CONSTRAINT TRIGGER prevent_deletion_if_children_exist_trigger
    AFTER DELETE ON types
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION prevent_deletion_if_children_exist();

-- 4. 부모 타입 유효기간 축소 방지 (자식 타입이 존재하는 기간을 벗어나는 수정 차단)
CREATE OR REPLACE FUNCTION prevent_invalid_parent_period_update()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF (NEW.effect_date_time <> OLD.effect_date_time OR NEW.expire_date_time <> OLD.expire_date_time) THEN
            IF EXISTS (
                SELECT 1
                FROM types AS child
                WHERE child.workspace = OLD.workspace
                  AND child.parent = OLD.id
                  AND (child.effect_date_time, child.expire_date_time) OVERLAPS (OLD.effect_date_time, OLD.expire_date_time)
                  AND (
                    GREATEST(child.effect_date_time, OLD.effect_date_time) < NEW.effect_date_time
                        OR LEAST(child.expire_date_time, OLD.expire_date_time) > NEW.expire_date_time
                    )
            ) THEN
                RAISE EXCEPTION 'Cannot modify parent type (id=%) as the new effective period [% → %] does not cover the overlapping periods with children.',
                    NEW.id, NEW.effect_date_time, NEW.expire_date_time;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_invalid_parent_period_update_trigger ON types;
CREATE CONSTRAINT TRIGGER prevent_invalid_parent_period_update_trigger
    AFTER UPDATE ON types
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION prevent_invalid_parent_period_update();

-- 5. 속성 참조 일관성 검사 (타입 A가 B를 참조할 때, A의 기간은 B의 존재 기간 내에 포함되어야 함)
CREATE OR REPLACE FUNCTION enforce_attribute_reference_consistency()
    RETURNS TRIGGER AS
$$
DECLARE
    owner_effect TIMESTAMPTZ;
    owner_expire TIMESTAMPTZ;
    ref_type_id  VARCHAR(255);
BEGIN
    -- 1. 참조 대상 추출
    ref_type_id := NEW.attribute_type ->> 'referenced_type';
    IF (ref_type_id IS NULL) THEN
        RETURN NEW;
    END IF;

    -- 2. 소유자 타입의 유효기간 조회
    SELECT effect_date_time, expire_date_time 
    INTO owner_effect, owner_expire
    FROM types
    WHERE workspace = NEW.workspace AND id = NEW.type_id AND version = NEW.type_version;

    -- 3. 참조 대상 타입의 존속성 및 커버리지 확인
    IF NOT EXISTS (
        SELECT 1
        FROM types
        WHERE workspace = NEW.workspace
          AND id = ref_type_id
          AND (effect_date_time, expire_date_time) OVERLAPS (owner_effect, owner_expire)
    ) THEN
        RAISE EXCEPTION 'Referenced type (id=%) does not exist during the owner type period.', ref_type_id;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM (
                 SELECT
                     MIN(effect_date_time) AS combined_start,
                     MAX(expire_date_time) AS combined_end,
                     SUM(CASE WHEN previous_expire_date_time IS NOT NULL AND previous_expire_date_time <> effect_date_time THEN 1 ELSE 0 END) AS gaps
                 FROM (
                          SELECT
                              effect_date_time,
                              expire_date_time,
                              LAG(expire_date_time) OVER (ORDER BY effect_date_time) AS previous_expire_date_time
                          FROM types
                          WHERE workspace = NEW.workspace
                            AND id = ref_type_id
                            AND (effect_date_time, expire_date_time) OVERLAPS (owner_effect, owner_expire)
                      ) sub
             ) merged
        WHERE gaps > 0
           OR combined_start > owner_effect
           OR combined_end < owner_expire
    ) THEN
        RAISE EXCEPTION 'Referenced type (id=%) has gaps or does not fully cover the owner type period [%, %]',
            ref_type_id, owner_effect, owner_expire;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS enforce_attribute_reference_consistency_trigger ON type_attributes;
CREATE CONSTRAINT TRIGGER enforce_attribute_reference_consistency_trigger
    AFTER INSERT OR UPDATE ON type_attributes
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_attribute_reference_consistency();

-- 6. 타입 유효기간 수정 시 속성 참조 일관성 재검증
CREATE OR REPLACE FUNCTION prevent_invalid_type_period_update_for_refs()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        -- 유효기간이 변경된 경우, 해당 타입의 모든 속성들이 참조하는 타입의 존재 여부 재검사
        IF (NEW.effect_date_time <> OLD.effect_date_time OR NEW.expire_date_time <> OLD.expire_date_time) THEN
            IF EXISTS (
                SELECT 1
                FROM type_attributes attr
                WHERE attr.workspace = NEW.workspace
                  AND attr.type_id = NEW.id
                  AND attr.type_version = NEW.version
                  AND attr.attribute_type ->> 'referenced_type' IS NOT NULL
                  AND (
                    SELECT COUNT(*) > 0
                    FROM (
                         SELECT
                             MIN(effect_date_time) AS combined_start,
                             MAX(expire_date_time) AS combined_end,
                             SUM(CASE WHEN previous_expire_date_time IS NOT NULL AND previous_expire_date_time <> effect_date_time THEN 1 ELSE 0 END) AS gaps
                         FROM (
                                  SELECT
                                      effect_date_time,
                                      expire_date_time,
                                      LAG(expire_date_time) OVER (ORDER BY effect_date_time) AS previous_expire_date_time
                                  FROM types
                                  WHERE workspace = NEW.workspace
                                    AND id = attr.attribute_type ->> 'referenced_type'
                                    AND (effect_date_time, expire_date_time) OVERLAPS (NEW.effect_date_time, NEW.expire_date_time)
                              ) sub
                    ) merged
                    WHERE gaps > 0
                       OR combined_start > NEW.effect_date_time
                       OR combined_end < NEW.expire_date_time
                  )
            ) THEN
                RAISE EXCEPTION 'Cannot modify type period as its attribute references would have gaps or lack coverage.';
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_invalid_type_period_update_for_refs_trigger ON types;
CREATE CONSTRAINT TRIGGER prevent_invalid_type_period_update_for_refs_trigger
    AFTER UPDATE ON types
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION prevent_invalid_type_period_update_for_refs();
