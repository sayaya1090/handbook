package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;

public class CreateBoxAction implements Action {
    private final Type box;
    private final TypeList subject;
    private final TypeListToUpsert toUpsert;
    @AssistedInject CreateBoxAction(TypeList typeList, TypeListToUpsert toUpsert, @Assisted Type box) {
        this.box = box;
        subject = typeList;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        subject.add(box);
        toUpsert.add(box);
    }
    @Override
    public void rollback() {
        subject.remove(box);
        toUpsert.remove(box);
    }
    @AssistedFactory
    interface CreateActionFactory {
        CreateBoxAction createBox(Type box);
    }
}
