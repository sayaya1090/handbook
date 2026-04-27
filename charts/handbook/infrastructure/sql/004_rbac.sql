-- 004_rbac.sql : RBAC 그룹-역할 매핑 테이블
-- 멱등: 모든 CREATE 에 IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS group_roles (
    workspace  UUID         NOT NULL,
    group_id   UUID         NOT NULL,
    role_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    PRIMARY KEY (group_id, role_name),
    CONSTRAINT fk_group_roles_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_roles_group
        FOREIGN KEY (group_id) REFERENCES "group"(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_group_roles_workspace_group
    ON group_roles (workspace, group_id);
