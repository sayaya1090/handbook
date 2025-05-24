package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.*;
import elemental2.dom.DomGlobal;

import java.util.stream.Collectors;

public class SaveAction implements Action {
    private final DocumentRepository typeRepository;
    private final DocumentListToDelete toDelete;
    private final DocumentListToUpsert toUpsert;
    @AssistedInject SaveAction(DocumentRepository typeRepository, DocumentListToDelete toDelete, DocumentListToUpsert toUpsert) {
        this.typeRepository = typeRepository;
        this.toDelete = toDelete;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        var deletes = toDelete.getValue();
        var upserts = toUpsert.getValue().stream().filter(s->!deletes.contains(s)).collect(Collectors.toSet());
        var delete = typeRepository.delete(toDelete.getValue()).tap(complete->toDelete.clear());
        var upsert = typeRepository.save(upserts).tap(complete->toUpsert.clear());
        delete.concatWith(upsert).subscribe(complete-> DomGlobal.alert("저장되었습니다."));
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
