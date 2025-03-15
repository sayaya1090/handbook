package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.BoxList;

public class DeleteBoxAction implements Action {
    private final CreateBoxAction reverseAction;
    @AssistedInject DeleteBoxAction(BoxList boxList, @Assisted Box... box) {
        reverseAction = new CreateBoxAction(boxList, box[0]);
    }
    @Override
    public void execute() {
        reverseAction.rollback();
    }

    @Override
    public void rollback() {
        reverseAction.execute();
    }

    @AssistedFactory
    interface DeleteActionFactory {
        DeleteBoxAction deleteBox(Box... box);
    }
}
