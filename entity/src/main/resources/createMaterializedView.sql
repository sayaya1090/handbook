CREATE MATERIALIZED VIEW public.type_attributes AS
WITH RECURSIVE
    -- 1. version_chain 재귀 구조 최적화
    version_chain AS (
        SELECT
            t.workspace,
            t.name,
            t.parent,
            t.version,
            t.effective_at,
            t.expire_at,
            0 AS depth
        FROM type t
        UNION ALL
        SELECT
            t.workspace,
            vc.name,
            t.parent,
            t.version,
            t.effective_at,
            t.expire_at,
            vc.depth + 1
        FROM version_chain vc
                 JOIN type t ON t.workspace=vc.workspace AND t.name = vc.parent
        WHERE t.parent IS NOT NULL
    ),
    -- 2. 유효한 transition_point만을 선택
    version_transitions AS (
        SELECT
            vc.workspace,
            vc.name,
            vc.effective_at AS transition_point,
            vc.version AS trigger_version
        FROM version_chain vc
        UNION ALL
        SELECT
            vc.workspace,
            vc.name,
            vc.expire_at AS transition_point,
            vc.version AS trigger_version
        FROM version_chain vc
    ),
    -- 3. transition_point를 정렬하고 유효 기간 정리
    ordered_transitions AS (
        SELECT
            vt.workspace,
            vt.name,
            vt.transition_point AS effective_at,
            LEAD(vt.transition_point) OVER (
                PARTITION BY vt.workspace, vt.name
                ORDER BY vt.transition_point
                ) AS expire_at
        FROM version_transitions vt
    ),
    -- 4. 버전 정보 필터링
    versions AS (
        SELECT DISTINCT ON (ot.workspace, ot.name, ot.effective_at)
            ot.workspace,
            ot.name,
            t.id,
            t.version,
            t.parent,
            ot.effective_at,
            ot.expire_at
        FROM ordered_transitions ot
        JOIN type t
          ON ot.workspace=t.workspace
          AND t.name = ot.name
          AND t.expire_at > ot.effective_at
          AND t.effective_at <= ot.effective_at
        WHERE ot.effective_at < ot.expire_at
        ORDER BY ot.workspace, ot.name, ot.effective_at, t.effective_at DESC
    ),
    -- 5. inheritance_chain 최적화
    inheritance_chain AS (
        SELECT
            v.workspace,
            v.name AS descendant_type,
            v.version,
            v.effective_at,
            v.expire_at,
            att.name AS attribute_name,
            att.attribute_type,
            att.description,
            att.nullable,
            att.value_type,
            att.reference_type,
            0 AS inheritance_depth,
            v.parent AS parent_type
        FROM versions v
                 LEFT JOIN attribute att ON att.workspace = v.workspace AND att.type = v.id
        UNION ALL
        SELECT
            ic.workspace,
            ic.descendant_type,
            t.version,
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
                 JOIN type t ON t.workspace = ic.workspace AND t.name = ic.parent_type
            AND t.effective_at <= ic.effective_at
            AND t.expire_at > ic.effective_at
                 LEFT JOIN attribute att ON att.workspace = t.workspace AND att.type = t.id
    ),
    -- 6. 필터링된 inheritance 결과에서 우선순위를 계산
    filtered_inheritance AS (
        SELECT
            ic.workspace,
            ic.descendant_type,
            ic.version,
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
                PARTITION BY ic.workspace, ic.descendant_type, ic.effective_at, ic.attribute_name
                ORDER BY ic.inheritance_depth
                ) AS priority
        FROM inheritance_chain ic
    )
-- 최종 결과 반환
SELECT
    workspace,
    descendant_type AS type,
    version,
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

CREATE INDEX idx1iajpy9hywmtq6kx97n618xud ON public.type_attributes USING btree (workspace, type, name);
CREATE INDEX idxpeomg1rph19vxib9crrljggdw ON public.type_attributes USING btree (workspace, effective_at, expire_at, type);
