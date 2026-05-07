package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Type;

/**
 * 타입의 유효기간(날짜)만 수정하는 액션.
 * 
 * <p><b>책임:</b> 현재 타입의 날짜 필드를 변경하고 Undo가 가능하도록 이전 상태를 저장한다.</p>
 */
public class EditTBoxDateAction implements Action {
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final Type before;
    private final Type after;

    public EditTBoxDateAction(TypeList typeList, ChangeTracker tracker, Type before, double newEffect, double newExpire) {
        this.typeList = typeList;
        this.tracker = tracker;
        this.before = before;
        this.after = Type.create(before.id(), before.version(), newEffect, newExpire);
        this.after.description(before.description());
        this.after.primitive(before.primitive());
        this.after.parent(before.parent());
        this.after.attributes(before.attributes());
    }

    @Override
    public void execute() {
        typeList.update(before, after);
        tracker.markChanged(after.key());
    }

    @Override
    public void rollback() {
        typeList.update(after, before);
        tracker.markChanged(before.key());
    }
}
