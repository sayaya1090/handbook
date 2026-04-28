-- 001_workspace.sql : workspace 도메인 기본 테이블
-- workspace-command 테스트 DDL 기반. FK/인덱스/감사 컬럼 추가.
-- 멱등: 모든 CREATE 에 IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS workspace (
    id                 UUID         NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    version            BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    last_modified_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_modified_by   UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "group" (
    id         UUID         NOT NULL,
    workspace  UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    PRIMARY KEY (id),
    UNIQUE (workspace, name),
    CONSTRAINT fk_group_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS group_member (
    id        UUID         NOT NULL,
    workspace UUID         NOT NULL,
    "group"   UUID         NOT NULL,
    member    UUID         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (workspace, "group", member),
    CONSTRAINT fk_group_member_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_member_group
        FOREIGN KEY ("group") REFERENCES "group"(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS webhooks (
    id         UUID         NOT NULL,
    workspace  UUID         NOT NULL,
    url        TEXT         NOT NULL,
    events     TEXT         NOT NULL DEFAULT '',
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    CONSTRAINT fk_webhooks_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_webhooks_workspace_active
    ON webhooks (workspace, active);
