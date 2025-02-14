CREATE OR REPLACE VIEW type_attributes AS
WITH RECURSIVE ranked_attributes AS (
    -- 1. 조상과 자식 관계 연결
    SELECT
        thc.descendant AS type_id,
        att.name AS attribute_name,
        att.description,
        att.nullable,
        att.primitive,
        att.value_type,
        att.attribute_type,
        att.reference_type,
        ROW_NUMBER() OVER (
            PARTITION BY thc.descendant, att.name
            ORDER BY
                CASE WHEN att.type = thc.descendant THEN 0 ELSE 1 END, -- 자식의 속성이 우선
                thc.depth ASC                                           -- 가까운 조상의 속성이 우선
            ) AS rank_num
    FROM
        type_hierarchy_closure thc
            JOIN
        attribute att
        ON
            thc.ancestor = att.type
) SELECT
     ra.type_id,
     ra.attribute_name,
     ra.description,
     ra.nullable,
     ra.primitive,
     ra.value_type,
     ra.attribute_type,
     ra.reference_type
FROM
    ranked_attributes ra
WHERE
    ra.rank_num = 1 -- 중복된 속성 중 우선순위가 가장 높은 속성만 가져옴