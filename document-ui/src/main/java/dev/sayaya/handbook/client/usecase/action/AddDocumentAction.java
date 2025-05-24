package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;

public class AddDocumentAction implements Action {
    private final Document document;
    private final DocumentList documents;
    @AssistedInject AddDocumentAction(@Assisted Document document, DocumentList documents) {
        this.document = document;
        this.documents = documents;
    }
    @Override
    public void execute() {
        documents.add(document);
    }
    @Override
    public void rollback() {
        documents.remove(document);
    }
    @AssistedFactory
    interface AddDocumentActionFactory {
        AddDocumentAction add(Document document);
    }
}
