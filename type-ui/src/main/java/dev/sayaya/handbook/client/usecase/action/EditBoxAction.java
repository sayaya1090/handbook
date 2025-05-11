package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;

class EditBoxAction implements Action {
    private final Type before;
    private final Type after;
    private final TypeList typeList;
    private final TypeListToUpsert toUpsert;
    @AssistedInject EditBoxAction(@Assisted("before") Type before, @Assisted("after") Type after, TypeList typeList, TypeListToUpsert toUpsert) {
        this.before = before;
        this.after = after;
        this.typeList = typeList;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        toUpsert.add(after);
        typeList.replace(before, after);
    }

    @Override
    public void rollback() {
        toUpsert.remove(after);
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
