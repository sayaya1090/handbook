package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;

public class CreateBoxAction implements Action {
    private final Type box;
    private final TypeList subject;
    @AssistedInject CreateBoxAction(TypeList typeList, @Assisted Type box) {
        this.box = box;
        subject = typeList;
    }
    @Override
    public void execute() {
        subject.add(box);
    }
    @Override
    public void rollback() {
        subject.remove(box);
    }
    @AssistedFactory
    interface CreateActionFactory {
        CreateBoxAction createBox(Type box);
    }
}
