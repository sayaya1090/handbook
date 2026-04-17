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
