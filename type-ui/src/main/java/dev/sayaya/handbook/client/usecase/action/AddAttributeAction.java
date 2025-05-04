package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

import java.util.LinkedList;

class AddAttributeAction implements Action {
    private final LinkedList<Attribute> before;
    private final LinkedList<Attribute> after;
    private final UpdatableBox element;
    AddAttributeAction(UpdatableBox element, Attribute value) {
        this.element = element;
        this.before = new LinkedList<>(element.box().attributes());
        this.after = new LinkedList<>(before);
        after.add(value);
    }
    @Override
    public void execute() {
        element.box().attributes(after);
        element.update();
    }

    @Override
    public void rollback() {
        element.box().attributes(before);
        element.update();
    }
}
