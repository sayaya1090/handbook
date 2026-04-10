package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/** 새 문서를 DocumentList에 추가하는 액션. */
public class AddDocumentAction implements Action {
    private final DocumentList documentList;
    private final DocumentValue newDoc;

    public AddDocumentAction(DocumentList documentList, DocumentValue newDoc) {
        this.documentList = documentList;
        this.newDoc = newDoc;
    }

    @Override
    public void execute() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        current.add(newDoc);
        documentList.next(current);
    }

    @Override
    public void rollback() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        current.remove(newDoc);
        documentList.next(current);
    }
}
