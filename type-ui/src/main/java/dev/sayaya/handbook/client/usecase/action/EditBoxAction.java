package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

public class EditBoxAction implements Action {
    private final Box before;
    private final Box after;
    private final UpdatableBox element;
    public EditBoxAction(UpdatableBox element, Box after) {
        this.element = element;
        this.before = element.box().toBuilder().build();
        this.after = after;
    }
    @Override
    public void execute() {
        element.box().name(after.name())
                .description(after.description())
                .x(after.x()).y(after.y())
                .width(after.width()).height(after.height());
        element.update();
    }

    @Override
    public void rollback() {
        element.box().name(before.name())
                .description(before.description())
                .x(before.x()).y(before.y())
                .width(before.width()).height(before.height());
        element.update();
    }
}
