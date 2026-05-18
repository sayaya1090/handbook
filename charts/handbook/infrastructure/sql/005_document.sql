-- 005_document.sql : document 도메인 테이블 (documents).
-- document-command 테스트 DDL 기반.

CREATE TABLE IF NOT EXISTS documents (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    workspace          UUID         NOT NULL,
    type               VARCHAR(255) NOT NULL,
    serial             VARCHAR(255) NOT NULL,
    effect_date_time   TIMESTAMPTZ  NOT NULL,
    expire_date_time   TIMESTAMPTZ  NOT NULL,
    data               JSONB        NOT NULL DEFAULT '{}'::jsonb,
    status             VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    create_date_time   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creator            VARCHAR(255),
    rev                BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_documents_workspace
        FOREIGN KEY (workspace) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT chk_documents_period
        CHECK (expire_date_time > effect_date_time)
);

CREATE INDEX IF NOT EXISTS idx_documents_ws_type_serial
    ON documents (workspace, type, serial);
CREATE INDEX IF NOT EXISTS idx_documents_ws_period
    ON documents (workspace, effect_date_time, expire_date_time);

-- -----------------------------------------------------------------------------
-- Data Integrity Triggers for Documents
-- -----------------------------------------------------------------------------

-- 1. 같은 문서(workspace, type, serial) 내에서는 유효기간 중복이 없어야 한다
CREATE OR REPLACE FUNCTION enforce_no_overlap_document_periods()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM documents
        WHERE workspace = NEW.workspace
          AND type = NEW.type
          AND serial = NEW.serial
          AND id <> NEW.id -- 자기 자신 제외
          AND (NEW.effect_date_time, NEW.expire_date_time) OVERLAPS (effect_date_time, expire_date_time)
    ) THEN
        RAISE EXCEPTION 'Overlapping periods are not allowed for document type: %, serial: %, effect: %, expire: %',
            NEW.type, NEW.serial, NEW.effect_date_time, NEW.expire_date_time;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS enforce_no_overlap_document_periods_trigger ON documents;
CREATE CONSTRAINT TRIGGER enforce_no_overlap_document_periods_trigger
    AFTER INSERT OR UPDATE ON documents
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION enforce_no_overlap_document_periods();
