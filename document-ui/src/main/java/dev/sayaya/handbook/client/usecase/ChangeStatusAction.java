package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 선택된 문서들의 상태를 일괄 변경하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute() 시 지정된 문서들의 status 필드를 새 상태로 변경하고,
 * rollback() 시 이전 상태로 복원하여 Undo를 지원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 문서 목록 상태 갱신 대상</li>
 *   <li>{@link DocumentValue} — 상태 변경 대상 문서 객체</li>
 *   <li>{@link Action} — Command 패턴 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 변경 전 상태를 Map으로 보관하여 문서별 원래 상태로 개별 복원이 가능하다.</p>
 */
public class ChangeStatusAction implements Action {
    private final DocumentList documentList;
    private final List<Integer> targetIndices;
    private final String newStatus;
    private final Map<Integer, String> previousStatuses = new LinkedHashMap<>();

    public ChangeStatusAction(DocumentList documentList, List<Integer> targetIndices, String newStatus) {
        this.documentList = documentList;
        this.targetIndices = new ArrayList<>(targetIndices);
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        previousStatuses.clear();
        for (int idx : targetIndices) {
            if (idx >= 0 && idx < current.size()) {
                DocumentValue doc = current.get(idx);
                previousStatuses.put(idx, doc.status);
                doc.status = newStatus;
            }
        }
        documentList.next(current);
    }

    @Override
    public void rollback() {
        List<DocumentValue> current = new ArrayList<>(documentList.getValue());
        for (Map.Entry<Integer, String> entry : previousStatuses.entrySet()) {
            int idx = entry.getKey();
            if (idx >= 0 && idx < current.size()) {
                current.get(idx).status = entry.getValue();
            }
        }
        documentList.next(current);
    }
}
