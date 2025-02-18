
-- 타입 상속 구조 자동 관리 트리거
-- 타입 추가 시 부모 계층 추가
CREATE OR REPLACE FUNCTION update_closure_on_insert()
    RETURNS TRIGGER AS $$
BEGIN
    -- 기존 상위 계층들과 새로운 타입 연결
    INSERT INTO public.type_hierarchy_closure (ancestor, descendant, depth)
    SELECT ancestor, NEW.id, depth + 1
    FROM public.type_hierarchy_closure
    WHERE descendant = NEW.parent;

    -- 자기 자신에 대한 정보 추가
    INSERT INTO public.type_hierarchy_closure (ancestor, descendant, depth)
    VALUES (NEW.id, NEW.id, 0);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성
CREATE TRIGGER trg_update_closure_on_insert AFTER INSERT ON type
FOR EACH ROW EXECUTE FUNCTION update_closure_on_insert();

-- 타입 삭제 시 기존 경로 삭제
CREATE OR REPLACE FUNCTION update_closure_on_delete()
    RETURNS TRIGGER AS $$
BEGIN
    -- 삭제된 타입과 연관된 모든 경로 삭제
    DELETE FROM public.type_hierarchy_closure
    WHERE ancestor = OLD.id OR descendant = OLD.id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성
CREATE TRIGGER trg_update_closure_on_delete AFTER DELETE ON type
FOR EACH ROW EXECUTE FUNCTION update_closure_on_delete();


CREATE OR REPLACE FUNCTION update_closure_on_update()
    RETURNS TRIGGER AS $$
BEGIN
    -- 1. 기존 계층 관계 삭제
    DELETE FROM public.type_hierarchy_closure
    WHERE descendant IN (
        SELECT id FROM type WHERE id = NEW.id OR parent = NEW.id
    );

    -- 2. 새로운 부모의 계층 관계 복사
    INSERT INTO public.type_hierarchy_closure (ancestor, descendant, depth)
    SELECT ancestor, NEW.id, depth + 1
    FROM public.type_hierarchy_closure
    WHERE descendant = NEW.parent;

    -- 3. 자기 자신 추가
    INSERT INTO public.type_hierarchy_closure (ancestor, descendant, depth)
    VALUES (NEW.id, NEW.id, 0);

    -- 4. 모든 자식 타입도 갱신
    WITH RECURSIVE descendants AS (
        SELECT id FROM type WHERE parent = NEW.id
        UNION
        SELECT t.id FROM type t JOIN descendants d ON t.parent = d.id
    )
    INSERT INTO public.type_hierarchy_closure (ancestor, descendant, depth)
    SELECT c.ancestor, d.id, c.depth + 1
    FROM public.type_hierarchy_closure c
             JOIN descendants d ON c.descendant = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_closure_on_update
    AFTER UPDATE ON type
    FOR EACH ROW
    WHEN (OLD.parent IS DISTINCT FROM NEW.parent)
EXECUTE FUNCTION update_closure_on_update();

