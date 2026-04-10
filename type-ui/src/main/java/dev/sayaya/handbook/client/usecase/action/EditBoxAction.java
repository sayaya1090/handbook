package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;

/**
 * 타입의 속성/메타데이터를 편집하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 TypeList에서 before를 after로 교체하고 ChangeTracker에 변경을 마킹한다.
 * rollback 시 after를 before로 복원하고 마킹을 해제한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeList} — 타입 갱신</li>
 *   <li>{@link ChangeTracker} — 변경 상태 마킹/해제</li>
 * </ul></p>
 * <p><b>주의:</b> 이름/버전 변경, 속성 추가/삭제/수정, 설명/부모 변경 등 모든 편집에 사용된다.</p>
 */
public class EditBoxAction implements Action {
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final TypeValue before;
    private final TypeValue after;

    public EditBoxAction(TypeList typeList, ChangeTracker tracker, TypeValue before, TypeValue after) {
        this.typeList = typeList;
        this.tracker = tracker;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        typeList.update(before, after);
        tracker.markChanged(after.key());
    }

    @Override
    public void rollback() {
        typeList.update(after, before);
        tracker.unmark(before.key());
    }
}
