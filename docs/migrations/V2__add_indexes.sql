CREATE INDEX IF NOT EXISTS idx_documents_workspace_type ON documents (workspace, type);
CREATE INDEX IF NOT EXISTS idx_documents_workspace_dates ON documents (workspace, effect_date_time, expire_date_time);
CREATE INDEX IF NOT EXISTS idx_types_workspace_dates ON types (workspace, effect_date_time, expire_date_time);
CREATE INDEX IF NOT EXISTS idx_type_attributes_type ON type_attributes (type_id, type_version);
