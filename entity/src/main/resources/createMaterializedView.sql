CREATE MATERIALIZED VIEW public.type_attributes AS
WITH ranked_attributes AS (
    SELECT
        thc.descendant AS type,
        att.name AS attribute_name,
        att.attribute_type,
        att.description,
        att.nullable,
        att.value_type,
        att.reference_type,
        thc.depth AS hierarchy_depth,
        GREATEST(tv_ancestor.effective_at, tv_descendant.effective_at) AS effective_start,
        LEAST(tv_ancestor.expire_at, tv_descendant.expire_at) AS effective_end,
        CASE WHEN tv_descendant.type::TEXT = thc.descendant::TEXT
                 THEN 'override'::TEXT
             ELSE 'inherited'::TEXT
            END AS attribute_source
    FROM type_hierarchy_closure thc JOIN
         type_validity tv_descendant ON tv_descendant.type::TEXT = thc.descendant::TEXT JOIN
         type_validity tv_ancestor ON tv_ancestor.type::TEXT = thc.ancestor::TEXT AND
                                      tv_ancestor.effective_at <= tv_descendant.expire_at AND
                                      (tv_ancestor.expire_at IS NULL OR tv_ancestor.expire_at >= tv_descendant.effective_at) JOIN
         type_definition td ON td.validity = tv_ancestor.id JOIN
         attribute att ON att.type_definition = td.id
    WHERE (
        NOT ( att.name::TEXT IN (
            SELECT child_att.name
            FROM attribute child_att
            WHERE child_att.type_definition IN (
                SELECT type_definition.id
                FROM type_definition
                WHERE type_definition.type::TEXT = thc.descendant::TEXT
            )
        ) ) OR tv_descendant.type::TEXT = thc.descendant::TEXT
    ) AND GREATEST(tv_ancestor.effective_at, tv_descendant.effective_at) <
          LEAST(tv_ancestor.expire_at, tv_descendant.expire_at)
), non_overlapping_attributes AS (
    SELECT
        ra.type,
        ra.attribute_name,
        ra.attribute_type,
        ra.description,
        ra.nullable,
        ra.value_type,
        ra.reference_type,
        ra.effective_start,
        ra.effective_end,
        ra.attribute_source,
        ROW_NUMBER() OVER (
            PARTITION BY ra.type, ra.attribute_name, ra.effective_start, ra.effective_end
            ORDER BY ra.hierarchy_depth
        ) AS row_num
    FROM ranked_attributes ra
), final_attributes AS (
    SELECT
        non_overlapping_attributes.type,
        non_overlapping_attributes.attribute_name,
        non_overlapping_attributes.attribute_type,
        non_overlapping_attributes.description,
        non_overlapping_attributes.nullable,
        non_overlapping_attributes.value_type,
        non_overlapping_attributes.reference_type,
        non_overlapping_attributes.effective_start AS effective_at,
        non_overlapping_attributes.effective_end AS expire_at,
        non_overlapping_attributes.attribute_source
    FROM non_overlapping_attributes
    WHERE non_overlapping_attributes.row_num = 1 -- 최종 필터 유지
) SELECT
    type,
    attribute_name AS name, -- 최종 필요한 값만 선택
    attribute_type,
    description,
    nullable,
    value_type,
    reference_type,
    effective_at,
    expire_at
FROM final_attributes;

CREATE INDEX IDXhi5nevk0iu6s5nkstdn9tobtv ON type_attributes (type, name);