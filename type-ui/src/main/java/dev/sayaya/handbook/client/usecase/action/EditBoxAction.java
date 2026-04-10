package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;

/** 타입의 속성/메타데이터를 편집한다. */
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
