-- Type 버전 관리
CREATE OR REPLACE FUNCTION update_last_type() RETURNS TRIGGER AS
$$
BEGIN
    -- 기존 데이터의 last를 false로 업데이트
    UPDATE type
    SET last = false
    WHERE workspace=NEW.workspace AND name=NEW.name AND version=NEW.version AND last=true;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER set_last_type
    BEFORE INSERT
    ON type
    FOR EACH ROW
EXECUTE FUNCTION update_last_type();

-- 같은 Type, version에 대해서는 last=true인 데이터가 유일해야 한다
-- last를 직접 변경하는 경우를 위한 검증
CREATE OR REPLACE FUNCTION enforce_unique_last_type() RETURNS TRIGGER AS
$$
BEGIN
    IF (NEW.last = TRUE) THEN
        IF EXISTS (
            SELECT 1 FROM type WHERE workspace = NEW.workspace AND id <> NEW.id AND name = NEW.name AND version=NEW.version AND last = TRUE
        ) THEN
            RAISE EXCEPTION 'Duplicate version: (%, %)', NEW.name, NEW.version;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE CONSTRAINT TRIGGER enforce_unique_last_type_trigger
    AFTER INSERT OR UPDATE ON type
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_unique_last_type();

-- last=true인 같은 타입 내에서는 유효기간 중복이 없어야 한다
CREATE OR REPLACE FUNCTION enforce_no_overlap_effective_periods()
    RETURNS TRIGGER AS
$$
BEGIN
    -- last=true인 데이터에 대해 기간 중복 검사를 수행
    IF (NEW.last = TRUE) THEN
        IF EXISTS (
            SELECT 1
            FROM type
            WHERE workspace = NEW.workspace
              AND name = NEW.name
              AND last = TRUE
              AND id <> NEW.id -- 자기 자신 제외
              AND (NEW.effective_at, NEW.expire_at) OVERLAPS (effective_at, expire_at) -- 기간 중복 확인
        ) THEN
            RAISE EXCEPTION 'Overlapping periods are not allowed for type: %, effective_at: %, expire_at: %',
                NEW.name, NEW.effective_at, NEW.expire_at;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER enforce_no_overlap_effective_periods_trigger
    AFTER INSERT OR UPDATE ON type
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_no_overlap_effective_periods();

-- 부모 타입의 존속성을 확인하고, 해당 기간 동안 gap 없이 NEW를 커버하는지 검사
-- 우선 NEW의 기간동안에 걸친 부모를 모두 가져오고,
-- 가져온 부모의 effective_at과 expire_at을 하나로 이어붙인 다음,
-- gap이 없는지 검사해보고, 이어붙인 기간이 NEW의 기간을 모두 커버하는지 확인
CREATE OR REPLACE FUNCTION enforce_parent_type_consistency()
    RETURNS TRIGGER AS
$$
BEGIN
    -- 부모가 설정되어 있는 경우 체크
    IF (NEW.parent IS NOT NULL) THEN
        -- 부모 존재 여부 확인
        IF NOT EXISTS (
            SELECT 1
            FROM type
            WHERE workspace = NEW.workspace
              AND name = NEW.parent
              AND last = TRUE
              AND (effective_at, expire_at) OVERLAPS (NEW.effective_at, NEW.expire_at)
        ) THEN
            RAISE EXCEPTION 'Parent type (name=%) does not exist.', NEW.parent;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM (
                     -- 겹치는 부모의 시작/종료 기간을 기반으로 집계
                     SELECT
                         MIN(effective_at) AS combined_start, -- 이어붙인 시작 시점
                         MAX(expire_at) AS combined_end,     -- 이어붙인 종료 시점
                         SUM(CASE WHEN previous_expire_at IS NOT NULL AND previous_expire_at <> effective_at THEN 1 ELSE 0 END) AS gaps
                     FROM (
                              SELECT
                                  effective_at,
                                  expire_at,
                                  LAG(expire_at) OVER (ORDER BY effective_at) AS previous_expire_at
                              FROM type
                              WHERE workspace = NEW.workspace
                                AND name = NEW.parent
                                AND last = TRUE
                                AND (effective_at, expire_at) OVERLAPS (NEW.effective_at, NEW.expire_at)
                          ) parent_subquery
                 ) merged_period
            WHERE gaps > 0 -- 겹치는 부모 기간 사이에 공백(gap)이 있으면 실패
               OR combined_start > NEW.effective_at -- 부모 시작 시점이 NEW의 시작보다 뒤이면 실패
               OR combined_end < NEW.expire_at      -- 부모 종료 시점이 NEW의 종료보다 앞이면 실패
        ) THEN
            RAISE EXCEPTION 'Parent type (name=%) is missing or has gaps or does not fully cover the period [%, %]',
                NEW.parent, NEW.effective_at, NEW.expire_at;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER enforce_parent_type_consistency_trigger
    AFTER INSERT OR UPDATE ON type
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_parent_type_consistency();

-- 타입 삭제 시, 상속한 자식 타입 부재 확인
-- last=true의 경우 해당 기간동안의 타입이 삭제대상뿐이므로, 삭제대상의 유효기간동안 겹치는 자식이 있는지 확인
CREATE OR REPLACE FUNCTION prevent_deletion_if_children_exist()
    RETURNS TRIGGER AS
$$
BEGIN
    -- 삭제 대상이 last=true인지 확인
    IF (OLD.last = TRUE) THEN
        -- 삭제 대상 부모의 유효 기간과 겹치는 자식이 있는지 확인
        IF EXISTS (
            SELECT 1
            FROM type AS child
            WHERE workspace = OLD.workspace
              AND child.parent = OLD.name
              AND child.last = TRUE
              AND (child.effective_at, child.expire_at) OVERLAPS (OLD.effective_at, OLD.expire_at)
        ) THEN
            RAISE EXCEPTION 'Cannot delete parent type (name=%, version=%) as it still has associated children during the period [% → %].',
                OLD.name, OLD.version, OLD.effective_at, OLD.expire_at;
        END IF;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER prevent_deletion_if_children_exist_trigger
    AFTER DELETE ON type
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION prevent_deletion_if_children_exist();

CREATE OR REPLACE FUNCTION prevent_invalid_parent_period_update()
    RETURNS TRIGGER AS
$$
DECLARE
    prev_record type%ROWTYPE;
BEGIN
    -- UPDATE의 경우 OLD 레코드 사용
    IF TG_OP = 'UPDATE' THEN
        prev_record := OLD;
        -- INSERT의 경우 같은 name, version 중 가장 최근 생성된 레코드를 OLD로 사용
    ELSIF TG_OP = 'INSERT' THEN
        SELECT *
        INTO prev_record
        FROM type
        WHERE workspace = NEW.workspace
          AND name = NEW.name
          AND version = NEW.version
          AND id<>NEW.id
        ORDER BY created_at DESC
        LIMIT 1;

        -- 이전 레코드가 없으면 검사 불필요. Child가 Parent보다 먼저 생성될 수 없음
        IF prev_record IS NULL THEN
            RETURN NEW;
        END IF;
    END IF;

    -- 유효기간이 변경된 경우에만 검사
    IF (NEW.effective_at <> prev_record.effective_at OR NEW.expire_at <> prev_record.expire_at) THEN
        IF EXISTS (
            SELECT 1
            FROM type AS child
            WHERE child.workspace = prev_record.workspace
              AND child.parent = prev_record.name
              AND child.last = TRUE
              AND (child.effective_at, child.expire_at) OVERLAPS (prev_record.effective_at, prev_record.expire_at)
              AND (
                GREATEST(child.effective_at, prev_record.effective_at) < NEW.effective_at
                    OR LEAST(child.expire_at, prev_record.expire_at) > NEW.expire_at
                )
        ) THEN
            RAISE EXCEPTION 'Cannot modify parent type (name=%) as the new effective period [% → %] does not cover the overlapping periods with children.',
                NEW.name, NEW.effective_at, NEW.expire_at;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 트리거 정의
CREATE CONSTRAINT TRIGGER prevent_invalid_parent_period_update_trigger
    AFTER INSERT OR UPDATE ON type
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION prevent_invalid_parent_period_update();
