package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

import java.util.List;

class EditAttributesAction implements Action {
    private final List<Attribute> before;
    private final List<Attribute> after;
    private final UpdatableBox element;
    private final TypeListToUpsert toUpsert;
    @AssistedInject EditAttributesAction(@Assisted UpdatableBox element, @Assisted("before") List<Attribute> before, @Assisted("after") List<Attribute> after, TypeListToUpsert toUpsert) {
        this.element = element;
        this.before = before;
        this.after = after;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        toUpsert.add(element.box());
        element.box().attributes(after);
        element.update();
    }
    @Override
    public void rollback() {
        toUpsert.remove(element.box());
        element.box().attributes(before);
        element.update();
    }
    @AssistedFactory
    interface AddAttributeActionFactory {
        EditAttributesAction _addAttribute(UpdatableBox element, @Assisted("before") List<Attribute> before, @Assisted("after") List<Attribute> after);
        default Action addAttribute(UpdatableBox element, List<Attribute> before, List<Attribute> after) {
            return _addAttribute(element, before, after);
        }
    }
}
