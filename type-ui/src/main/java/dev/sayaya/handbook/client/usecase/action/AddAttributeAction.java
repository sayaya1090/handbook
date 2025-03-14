package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

import java.util.LinkedList;

public class AddAttributeAction implements Action {
    private final LinkedList<Value> before;
    private final LinkedList<Value> after;
    private final UpdatableBox element;
    public AddAttributeAction(UpdatableBox element, Value value) {
        this.element = element;
        this.before = new LinkedList<>(element.box().values());
        this.after = new LinkedList<>(before);
        after.add(value);
    }
    @Override
    public void execute() {
        element.box().values(after);
        element.update();
    }

    @Override
    public void rollback() {
        element.box().values(before);
        element.update();
    }
}
