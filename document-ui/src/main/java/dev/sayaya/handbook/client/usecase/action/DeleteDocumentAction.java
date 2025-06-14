package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DeleteDocumentAction implements Action {
    private final Map<Document, Document> values = new HashMap<>();
    private final DocumentList documents;
    @AssistedInject DeleteDocumentAction(@Assisted List<Document> targets, DocumentList documents) {
        targets.forEach(target ->{
            if(target.isDelete() == Document.DocumentDeleteState.DELETE) return;
            var after = target.toBuilder().isDelete(Document.DocumentDeleteState.DELETE);
            values.put(target, after.build());
        });
        this.documents = documents;
    }
    @Override
    public void execute() {
        documents.replaces(values);
    }

    @Override
    public void rollback() {
        var reverse = new HashMap<Document, Document>();
        values.forEach((k,v) -> reverse.put(v, k));
        documents.replaces(reverse);
    }
    @AssistedFactory interface DeleteDocumentActionFactory {
        DeleteDocumentAction _delete(List<Document> targets);
        default Action delete(List<Document> targets) {
            return _delete(targets);
        }
    }
}