package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.UpdatableType;

import java.util.Arrays;

class DeleteBoxAction extends ComplexAction {
    private final UpdatableType[] updatables;
    @AssistedInject DeleteBoxAction(TypeList typeList, @Assisted UpdatableType... types) {
        super(
            Arrays.stream(types)
                .map(UpdatableType::value)
                .map(type -> new EditBoxAction(type, type.toBuilder().state(Type.TypeState.DELETE).build(), typeList))
                .toArray(Action[]::new)
        );
        this.updatables = types;
    }
    @Override
    public void execute() {
        super.execute();
        for(UpdatableType updatable : updatables) updatable.update();
    }
    @Override
    public void rollback() {
        super.rollback();
        for(UpdatableType updatable : updatables) updatable.update();
    }
    @AssistedFactory
    interface DeleteActionFactory {
        DeleteBoxAction _deleteBox(UpdatableType... box);
        default Action deleteBox(UpdatableType... box) {
            return _deleteBox(box);
        }
    }
}
