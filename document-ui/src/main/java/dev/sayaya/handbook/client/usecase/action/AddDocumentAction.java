package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.DocumentListToUpsert;
import elemental2.dom.DomGlobal;

public class AddDocumentAction implements Action {
    private final Document document;
    private final DocumentList documents;
    private final DocumentListToUpsert toUpsert;
    @AssistedInject AddDocumentAction(@Assisted Document document, DocumentList documents, DocumentListToUpsert toUpsert) {
        this.document = document;
        this.documents = documents;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        DomGlobal.console.log("AddDocumentAction.execute");
        documents.add(document);
        toUpsert.add(document);
    }
    @Override
    public void rollback() {
        documents.remove(document);
        toUpsert.remove(document);
    }
    @AssistedFactory
    interface AddDocumentActionFactory {
        AddDocumentAction add(Document document);
    }
}
