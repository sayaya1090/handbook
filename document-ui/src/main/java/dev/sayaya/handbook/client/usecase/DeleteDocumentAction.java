package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/** 선택된 문서를 DocumentList에서 제거하는 액션. */
public class DeleteDocumentAction implements Action {
    private final DocumentList documentList;
    private final List<DocumentValue> deleted;
    private final List<Integer> deletedIndices;

    public DeleteDocumentAction(DocumentList documentList, List<DocumentValue> deleted, List<Integer> deletedIndices) {
        this.documentList = documentList;
        this.deleted = new ArrayList<>(deleted);
        this.deletedIndices = new ArrayList<>(deletedIndices);
    }

    @Override
    public void execute() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        current.removeAll(deleted);
        documentList.next(current);
    }

    @Override
    public void rollback() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        for (int i = 0; i < deleted.size(); i++) {
            int idx = Math.min(deletedIndices.get(i), current.size());
            current.add(idx, deleted.get(i));
        }
        documentList.next(current);
    }
}
