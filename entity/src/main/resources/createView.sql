CREATE OR REPLACE VIEW public.document_with_validation AS
    SELECT d.workspace, d.id, d.type, d.serial, d.created_at, d.created_by, u.name AS creator_name,
           d.effective_at, d.expire_at, d.data, d.last,
           v.created_at AS validation_requested_at, v.started_at AS validation_started_at,
           v.status AS validation_status, v.priority AS validation_priority,
           v.results AS validation_results, v.retry_count AS validation_retry, v.last_error AS validation_error
    FROM public.document d
    LEFT JOIN public.user u ON d.created_by=u.id
    LEFT JOIN public.validation_task v ON v.workspace=d.workspace AND v.document=d.id AND v.last=true
