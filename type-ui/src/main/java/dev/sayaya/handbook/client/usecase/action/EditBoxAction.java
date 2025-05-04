package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

class EditBoxAction implements Action {
    private final Type before;
    private final Type after;
    private final UpdatableBox element;
    private final TypeListToUpsert toUpsert;
    @AssistedInject EditBoxAction(@Assisted UpdatableBox element, @Assisted Type after, TypeListToUpsert toUpsert) {
        this.element = element;
        this.before = element.box().toBuilder().build();
        this.after = after;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        element.box().copyFrom(after);
        element.update();
        toUpsert.add(after);
    }

    @Override
    public void rollback() {
        element.box().copyFrom(before);
        element.update();
        toUpsert.remove(after);
    }
    @AssistedFactory
    interface EditBoxActionFactory {
        EditBoxAction _editBox(UpdatableBox element, Type after);
        default Action editBox(UpdatableBox element, Type after) {
            return _editBox(element, after);
        }
    }
}
