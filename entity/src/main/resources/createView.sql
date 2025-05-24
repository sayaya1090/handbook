CREATE OR REPLACE VIEW document_with_user AS
    SELECT d.workspace, d.id, d.type, d.serial, d.created_at, d.created_by, u.name as creator_name,
           d.effective_at, d.expire_at, d.data, d.last
    FROM public.document d
    LEFT JOIN public.user u ON d.created_by=u.id

