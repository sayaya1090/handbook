package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 선택된 문서를 DocumentList에서 제거하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute() 시 지정된 문서들을 목록에서 제거하고,
 * rollback() 시 원래 인덱스 위치에 다시 삽입하여 Undo를 지원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 문서 목록 상태 갱신 대상</li>
 *   <li>{@link dev.sayaya.handbook.domain.Document} — 삭제 대상 문서 객체</li>
 *   <li>{@link dev.sayaya.handbook.domain.Action} — Command 패턴 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 삭제된 문서와 인덱스를 복사본으로 보관하므로 rollback 시 원래 위치에 복원된다.
 * 인덱스가 현재 리스트 크기를 초과하면 끝에 삽입된다.</p>
 */
public class DeleteDocumentAction implements Action {
    private final DocumentList documentList;
    private final List<Document> deleted;
    private final List<Integer> deletedIndices;

    public DeleteDocumentAction(DocumentList documentList, List<Document> deleted, List<Integer> deletedIndices) {
        this.documentList = documentList;
        this.deleted = new ArrayList<>(deleted);
        this.deletedIndices = new ArrayList<>(deletedIndices);
    }

    @Override
    public void execute() {
        List<Document> current = new ArrayList<>(documentList.getValue());
        current.removeAll(deleted);
        documentList.next(current);
    }

    @Override
    public void rollback() {
        List<Document> current = new ArrayList<>(documentList.getValue());
        for (int i = 0; i < deleted.size(); i++) {
            int idx = Math.min(deletedIndices.get(i), current.size());
            current.add(idx, deleted.get(i));
        }
        documentList.next(current);
    }
}
