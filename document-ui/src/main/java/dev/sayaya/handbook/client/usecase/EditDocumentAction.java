package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/** 문서 셀 값 변경 액션. before/after 스냅샷으로 undo/redo를 지원한다. */
public class EditDocumentAction implements Action {
    private final DocumentList documentList;
    private final int index;
    private final String field;
    private final String beforeValue;
    private final String afterValue;

    public EditDocumentAction(DocumentList documentList, int index, String field, String beforeValue, String afterValue) {
        this.documentList = documentList;
        this.index = index;
        this.field = field;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    @Override
    public void execute() {
        applyValue(afterValue);
    }

    @Override
    public void rollback() {
        applyValue(beforeValue);
    }

    private void applyValue(String value) {
        List<DocumentValue> docs = documentList.getValue();
        if (index >= 0 && index < docs.size()) {
            DocumentValue doc = docs.get(index);
            doc.data.set(field, value);
            documentList.next(new ArrayList<>(docs));
        }
    }
}
