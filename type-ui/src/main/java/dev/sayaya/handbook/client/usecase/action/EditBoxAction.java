package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;

class EditBoxAction implements Action {
    private final Type before;
    private final Type after;
    private final TypeList typeList;
    @AssistedInject EditBoxAction(@Assisted("before") Type before, @Assisted("after") Type after, TypeList typeList) {
        this.before = before;
        this.after = after;
        this.typeList = typeList;
    }
    @Override
    public void execute() {
        typeList.replace(before, after);
    }

    @Override
    public void rollback() {
        typeList.replace(after, before);
    }

    @AssistedFactory
    interface EditBoxActionFactory {
        EditBoxAction _editBox(@Assisted("before") Type before, @Assisted("after") Type after);
        default Action editBox(Type before, Type after) {
            return _editBox(before, after);
        }
    }
}
