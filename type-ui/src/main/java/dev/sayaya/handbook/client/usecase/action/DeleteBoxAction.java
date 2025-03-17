package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.BoxList;

import java.util.Arrays;

public class DeleteBoxAction extends ComplexAction {
    @AssistedInject DeleteBoxAction(BoxList boxList, @Assisted Box... box) {
        super(pipeline(boxList, box));
    }
    private static Action[] pipeline(BoxList boxList, Box... boxes) {
        return Arrays.stream(boxes).map(box->new CreateBoxAction(boxList, box)).map(ReverseAction::new).toArray(Action[]::new);
    }

    @AssistedFactory
    interface DeleteActionFactory {
        DeleteBoxAction deleteBox(Box... box);
    }

    private record ReverseAction(Action action) implements Action {
        @Override
            public void execute() {
                action.rollback();
            }

            @Override
            public void rollback() {
                action.execute();
            }
        }
}
