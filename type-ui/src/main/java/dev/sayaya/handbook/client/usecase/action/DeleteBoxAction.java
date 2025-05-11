package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeListToDelete;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;

import java.util.HashSet;
import java.util.Set;

public class DeleteBoxAction implements Action {
    private final TypeList subject;
    private final TypeListToDelete toDelete;
    private final TypeListToUpsert toUpsert;
    private final Type[] boxes;
    private final Set<Type> tmp = new HashSet<>();
    @AssistedInject DeleteBoxAction(TypeList typeList, TypeListToDelete toDelete, TypeListToUpsert toUpsert, @Assisted Type... boxes) {
        this.subject = typeList;
        this.toDelete = toDelete;
        this.toUpsert = toUpsert;
        this.boxes = boxes;
    }

    @Override
    public void execute() {
        var toUpsert = this.toUpsert.getValue();
        for(var type: boxes) {
            toDelete.add(type);
            if(toUpsert.contains(type)) {
                tmp.add(type);
                this.toUpsert.remove(type);
            }
        }
        subject.remove(boxes);
    }

    @Override
    public void rollback() {
        for(var type: boxes) toDelete.remove(type);
        for(var type: tmp) this.toUpsert.add(type);
        subject.add(boxes);
    }

    @AssistedFactory
    interface DeleteActionFactory {
        DeleteBoxAction deleteBox(Type... box);
    }

    private record DeleteMonoBoxAction(Type box, TypeListToDelete toDelete) implements Action {
        @Override
        public void execute() {
            toDelete.add(box);
        }
        @Override
        public void rollback() {
            toDelete.remove(box);
        }
    }
}
