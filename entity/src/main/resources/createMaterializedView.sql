CREATE MATERIALIZED VIEW public.type_attributes AS
WITH RECURSIVE
    -- 1. validity_chain 재귀 구조 최적화
    validity_chain AS (
        SELECT
            t.id AS type,
            t.parent,
            tv.id AS validity_id,
            tv.effective_at,
            tv.expire_at,
            0 AS depth
        FROM type t
                 JOIN type_validity tv ON t.id = tv.type
        UNION ALL
        SELECT
            vc.type,
            t.parent,
            tv.id AS validity_id,
            tv.effective_at,
            tv.expire_at,
            vc.depth + 1
        FROM validity_chain vc
                 JOIN type t ON t.id = vc.parent
                 JOIN type_validity tv ON t.id = tv.type
        WHERE t.parent IS NOT NULL
    ),
    -- 2. 유효한 transition_point만을 선택
    validity_transitions AS (
        SELECT
            vc.type,
            vc.effective_at AS transition_point,
            vc.validity_id AS trigger_validity_id
        FROM validity_chain vc
        UNION ALL
        SELECT
            vc.type,
            vc.expire_at AS transition_point,
            vc.validity_id AS trigger_validity_id
        FROM validity_chain vc
    ),
    -- 3. transition_point를 정렬하고 유효 기간 정리
    ordered_transitions AS (
        SELECT
            vt.type,
            vt.transition_point AS effective_at,
            LEAD(vt.transition_point) OVER (
                PARTITION BY vt.type
                ORDER BY vt.transition_point
                ) AS expire_at
        FROM validity_transitions vt
    ),
    -- 4. 버전 정보 필터링
    versions AS (
        SELECT DISTINCT ON (ot.type, ot.effective_at)
            ot.type,
            tv.id AS validity_id,
            ot.effective_at,
            ot.expire_at
        FROM ordered_transitions ot
                 JOIN type_validity tv ON tv.type = ot.type
        WHERE ot.effective_at < ot.expire_at
          AND tv.effective_at <= ot.effective_at
          AND tv.expire_at > ot.effective_at
        ORDER BY ot.type, ot.effective_at, tv.effective_at DESC
    ),
    -- 5. inheritance_chain 최적화
    inheritance_chain AS (
        SELECT
            v.type AS descendant_type,
            v.effective_at,
            v.expire_at,
            att.name AS attribute_name,
            att.attribute_type,
            att.description,
            att.nullable,
            att.value_type,
            att.reference_type,
            0 AS inheritance_depth,
            t.parent AS parent_type
        FROM versions v
                 JOIN type t ON t.id = v.type
                 JOIN type_definition td ON td.validity = v.validity_id
                 LEFT JOIN attribute att ON att.type_definition = td.id
        UNION ALL
        SELECT
            ic.descendant_type,
            ic.effective_at,
            ic.expire_at,
            att.name,
            att.attribute_type,
            att.description,
            att.nullable,
            att.value_type,
            att.reference_type,
            ic.inheritance_depth + 1,
            t.parent
        FROM inheritance_chain ic
                 JOIN type t ON t.id = ic.parent_type
                 JOIN type_validity tv ON t.id = tv.type
            AND tv.effective_at <= ic.effective_at
            AND tv.expire_at > ic.effective_at
                 JOIN type_definition td ON td.validity = tv.id
                 LEFT JOIN attribute att ON att.type_definition = td.id
    ),
    -- 6. 필터링된 inheritance 결과에서 우선순위를 계산
    filtered_inheritance AS (
        SELECT
            ic.descendant_type,
            ic.effective_at,
            ic.expire_at,
            ic.attribute_name,
            ic.attribute_type,
            ic.description,
            ic.nullable,
            ic.value_type,
            ic.reference_type,
            ic.inheritance_depth,
            ROW_NUMBER() OVER (
                PARTITION BY ic.descendant_type, ic.effective_at, ic.attribute_name
                ORDER BY ic.inheritance_depth
                ) AS priority
        FROM inheritance_chain ic
    )
-- 최종 결과 반환
SELECT
    descendant_type AS type,
    attribute_type,
    attribute_name AS name,
    description,
    nullable,
    value_type,
    reference_type,
    effective_at,
    expire_at
FROM filtered_inheritance
WHERE priority = 1;