package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;

/** 타입을 삭제 마킹한다. */
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
