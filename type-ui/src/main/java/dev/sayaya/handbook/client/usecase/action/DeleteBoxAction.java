package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;

/**
 * 타입을 삭제 마킹하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 ChangeTracker에 삭제를 마킹하고 TypeList에서 타입을 제거한다.
 * rollback 시 타입을 복원하고 이전 변경 상태(NOT_CHANGED/CHANGED)에 따라 마킹을 복원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeList} — 타입 제거/복원</li>
 *   <li>{@link ChangeTracker} — 삭제 마킹 및 이전 상태 보존</li>
 * </ul></p>
 * <p><b>주의:</b> rollback 시 이전 상태(previousState)를 기반으로 마킹을 복원하므로,
 * 이미 변경된 타입을 삭제했다가 undo하면 변경 상태가 유지된다.</p>
 */
public class DeleteBoxAction implements Action {
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final TypeValue type;
    private ChangeTracker.ChangeState previousState;

    public DeleteBoxAction(TypeList typeList, ChangeTracker tracker, TypeValue type) {
        this.typeList = typeList;
        this.tracker = tracker;
        this.type = type;
    }

    @Override
    public void execute() {
        previousState = tracker.getState(type.key());
        tracker.markDeleted(type.key());
        typeList.remove(type);
    }

    @Override
    public void rollback() {
        typeList.add(type);
        if (previousState == ChangeTracker.ChangeState.NOT_CHANGED) tracker.unmark(type.key());
        else tracker.markChanged(type.key());
    }
}
