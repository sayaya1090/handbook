package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.*;
import elemental2.dom.DomGlobal;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final TypeListToDelete toDelete;
    private final TypeListToUpsert toUpsert;

    @AssistedInject SaveAction(TypeRepository typeRepository, TypeListToDelete toDelete, TypeListToUpsert toUpsert) {
        this.typeRepository = typeRepository;
        this.toDelete = toDelete;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        var deletes = toDelete.getValue();
        var upserts = toUpsert.getValue().stream().filter(s->!deletes.contains(s)).collect(Collectors.toSet());
        typeRepository.save(deletes, upserts).subscribe(complete->{
            DomGlobal.alert("저장되었습니다.");
        });
    }
    @Override
    public void rollback() {
        throw new UnsupportedOperationException("rollback");
    }
    @AssistedFactory
    interface SaveActionFactory {
        SaveAction save();
    }
}
