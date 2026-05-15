package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Type;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 타입의 속성/메타데이터를 편집하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 TypeList에서 before를 after로 교체하고 ChangeTracker에 변경을 마킹한다.
 * 각 필드별 변경 사항을 상세히 추적하여 UI 하이라이트를 지원한다.
 * rollback 시 after를 before로 복원하고 마킹을 해제한다.</p>
 */
public class EditBoxAction implements Action {
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final Type before;
    private final Type after;

    public EditBoxAction(TypeList typeList, ChangeTracker tracker, Type before, Type after) {
        this.typeList = typeList;
        this.tracker = tracker;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        typeList.update(before, after);
        tracker.trackChange(after.key(), before, after, this::isSameType);
        markGranularChanges(before, after);
    }

    @Override
    public void rollback() {
        typeList.update(after, before);
        // Undo 시에도 역순 비교를 통해 상태 복원 (tracker가 원래 값을 기억하고 있음)
        tracker.trackChange(before.key(), before, before, this::isSameType);
        markGranularChanges(after, before); // 역순으로 비교하여 원상 복귀 체크
    }

    private void markGranularChanges(Type b, Type a) {
        String key = a.key();
        tracker.trackChange(key + ":id", b.id(), a.id());
        tracker.trackChange(key + ":version", b.version(), a.version());
        tracker.trackChange(key + ":description", b.description(), a.description());
        
        Set<String> beforeAttrs = new HashSet<>();
        if (b.attributes() != null) for (Attribute attr : b.attributes()) beforeAttrs.add(attr.name());
        Set<String> afterAttrs = new HashSet<>();
        if (a.attributes() != null) for (Attribute attr : a.attributes()) afterAttrs.add(attr.name());
        
        // 속성 변경 추적
        Set<String> allAttrs = new HashSet<>(beforeAttrs);
        allAttrs.addAll(afterAttrs);
        
        for (String attrName : allAttrs) {
            Attribute oldAttr = findAttribute(b, attrName);
            Attribute newAttr = findAttribute(a, attrName);
            tracker.trackChange(key + ":attr:" + attrName, oldAttr, newAttr, this::isSameAttribute);
        }
    }

    private boolean isSameType(Type b, Type a) {
        if (b == a) return true;
        if (b == null || a == null) return false;
        
        if (!Objects.equals(b.id(), a.id())) return false;
        if (!Objects.equals(b.version(), a.version())) return false;
        if (!Objects.equals(b.description(), a.description())) return false;
        
        Attribute[] bAttrs = b.attributes();
        Attribute[] aAttrs = a.attributes();
        if (bAttrs == null && aAttrs == null) return true;
        if (bAttrs == null || aAttrs == null || bAttrs.length != aAttrs.length) return false;
        
        for (int i = 0; i < bAttrs.length; i++) {
            if (!isSameAttribute(bAttrs[i], aAttrs[i])) return false;
        }
        return true;
    }

    private Attribute findAttribute(Type t, String name) {
        if (t.attributes() == null) return null;
        for (Attribute attr : t.attributes()) {
            if (Objects.equals(attr.name(), name)) return attr;
        }
        return null;
    }

    private boolean isSameAttribute(Attribute b, Attribute a) {
        if (b == a) return true;
        if (b == null || a == null) return false;
        return Objects.equals(b.name(), a.name()) &&
               Objects.equals(b.description(), a.description()) &&
               Objects.equals(b.type(), a.type()) &&
               b.order() == a.order() &&
               b.nullable() == a.nullable();
    }
}
