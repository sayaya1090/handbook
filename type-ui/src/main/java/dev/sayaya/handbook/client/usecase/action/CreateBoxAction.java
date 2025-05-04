package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeListEditing;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;
import elemental2.core.JsArray;

import java.util.Arrays;

public class CreateBoxAction implements Action {
    private final Type box;
    private final TypeListEditing subject;
    private final TypeListToUpsert toUpsert;
    @AssistedInject CreateBoxAction(TypeListEditing typeListEditing, TypeListToUpsert toUpsert, @Assisted Type box) {
        this.box = box;
        subject = typeListEditing;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        var array = JsArray.asJsArray(subject.getValue());
        array.push(box);
        var next = array.asList().stream().toArray(Type[]::new);
        subject.next(next);
        toUpsert.add(box);
    }
    @Override
    public void rollback() {
        var next = Arrays.stream(subject.getValue()).filter(s->s!=box).toArray(Type[]::new);
        subject.next(next);
        toUpsert.remove(box);
    }
    @AssistedFactory
    interface CreateActionFactory {
        CreateBoxAction createBox(Type box);
    }
}
