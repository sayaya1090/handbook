package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeListEditing;
import dev.sayaya.handbook.client.usecase.TypeListToDelete;
import elemental2.core.JsArray;

import java.util.Arrays;

public class DeleteBoxAction extends ComplexAction {
    @AssistedInject DeleteBoxAction(TypeListEditing typeListEditing, TypeListToDelete toDelete, @Assisted Type... box) {
        super(pipeline(typeListEditing, toDelete, box));
    }
    private static Action[] pipeline(TypeListEditing typeListEditing, TypeListToDelete toDelete, Type... boxes) {
        return Arrays.stream(boxes).map(box->new DeleteMonoBoxAction(box, typeListEditing, toDelete)).toArray(Action[]::new);
    }

    @AssistedFactory
    interface DeleteActionFactory {
        DeleteBoxAction deleteBox(Type... box);
    }

    private record DeleteMonoBoxAction(
            Type box,
            TypeListEditing subject,
            TypeListToDelete toDelete
    ) implements Action {
        @Override
        public void execute() {
            var next = Arrays.stream(subject.getValue()).filter(s->s!=box).toArray(Type[]::new);
            subject.next(next);
            toDelete.add(box);
        }
        @Override
        public void rollback() {
            var array = JsArray.asJsArray(subject.getValue());
            array.push(box);
            var next = array.asList().stream().toArray(Type[]::new);
            subject.next(next);
            toDelete.remove(box);
        }
    }
}
