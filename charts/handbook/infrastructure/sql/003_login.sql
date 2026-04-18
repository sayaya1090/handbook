-- 003_login.sql : login 도메인 users 테이블.
-- OAuth2 로그인 시 lookup-or-create 되는 내부 사용자 레코드.
-- (provider, account) 가 외부 ID 튜플, id(UUID) 가 내부 영구 식별자이자 JWT sub 클레임.
-- Phase 1a (2026-04-18) 로 내부 UUID 를 JWT sub 에 심는 계약이 고정됨 — 소비자
-- (persist-workspace 등) 는 이 id 를 user_id 로 참조한다. 멱등 CREATE IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS users (
    id               UUID         NOT NULL,
    provider         VARCHAR(64)  NOT NULL,
    account          VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    state            VARCHAR(32)  NOT NULL DEFAULT 'ACTIVATED',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login_at    TIMESTAMPTZ,
    last_modified_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    CONSTRAINT uq_users_provider_account UNIQUE (provider, account)
);

-- findByProviderAndAccount 가 핫패스(매 로그인). UNIQUE 제약이 인덱스를 만들지만
-- 의도를 드러내기 위해 명시적으로 남긴다 — 기존 DDL 컨벤션과 동일.
CREATE INDEX IF NOT EXISTS idx_users_provider_account ON users (provider, account);
