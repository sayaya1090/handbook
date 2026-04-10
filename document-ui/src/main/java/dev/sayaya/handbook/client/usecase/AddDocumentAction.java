package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 새 문서를 DocumentList에 추가하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute() 시 새 {@link dev.sayaya.handbook.client.domain.DocumentValue}를
 * {@link DocumentList} 끝에 추가하고, rollback() 시 해당 문서를 제거하여 Undo를 지원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 문서 목록 상태 갱신 대상</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.DocumentValue} — 추가할 문서 객체</li>
 *   <li>{@link dev.sayaya.handbook.domain.Action} — Command 패턴 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> rollback은 객체 참조 비교(remove)로 삭제하므로,
 * 동일한 newDoc 인스턴스가 리스트에 존재해야 정상 동작한다.</p>
 */
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
