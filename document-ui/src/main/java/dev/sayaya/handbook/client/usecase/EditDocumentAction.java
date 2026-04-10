package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 문서 셀 값 변경 Command 패턴 액션.
 *
 * <p><b>책임:</b> 특정 문서의 특정 필드 값을 변경한다. before/after 값을 보관하여
 * execute() 시 afterValue를, rollback() 시 beforeValue를 적용함으로써
 * Undo/Redo를 지원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 문서 목록 상태 갱신 대상</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.DocumentValue} — 편집 대상 문서의 data 맵</li>
 *   <li>{@link dev.sayaya.handbook.domain.Action} — Command 패턴 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> index 기반으로 문서를 참조하므로, 중간에 문서가 추가/삭제되면
 * 잘못된 문서가 편집될 수 있다. ActionManager의 순차 실행에 의존한다.</p>
 */
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
