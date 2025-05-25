package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.*;
import elemental2.dom.DomGlobal;

import java.util.stream.Collectors;

class SaveAction implements Action {
    private final DocumentRepository documentRepository;
    private final DocumentListToDelete toDelete;
    private final DocumentListToUpsert toUpsert;
    @AssistedInject SaveAction(DocumentRepository documentRepository, DocumentListToDelete toDelete, DocumentListToUpsert toUpsert) {
        this.documentRepository = documentRepository;
        this.toDelete = toDelete;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        var deletes = toDelete.getValue();
        var upserts = toUpsert.getValue().stream().filter(s->!deletes.contains(s)).collect(Collectors.toSet());
        var delete = documentRepository.delete(toDelete.getValue());
        var upsert = documentRepository.save(upserts);
        delete.combineLatest(upsert).subscribe(complete-> DomGlobal.alert("저장되었습니다."));
    }
    @Override
    public void rollback() {
        throw new UnsupportedOperationException("rollback");
    }
    @AssistedFactory
    interface SaveActionFactory {
        SaveAction _save();
        default Action save() { return _save(); }
    }
}
