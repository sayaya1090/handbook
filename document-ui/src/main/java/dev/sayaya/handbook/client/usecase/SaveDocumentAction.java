package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 문서를 저장소에 저장하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute() 시 현재 문서 목록의 스냅샷을 저장하고
 * {@link DocumentRepository}를 통해 서버에 저장 요청을 보낸다.
 * 저장 성공 시 {@link dev.sayaya.handbook.client.components.ActionManager#clear()}로
 * Undo/Redo 스택을 초기화한다. rollback() 시 스냅샷으로 문서 목록을 복원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 저장할 문서 목록 조회 및 rollback 시 복원</li>
 *   <li>{@link DocumentRepository} — 서버 저장 API 호출</li>
 *   <li>{@link dev.sayaya.handbook.client.components.ActionManager} — 저장 후 스택 초기화</li>
 *   <li>{@link dev.sayaya.handbook.domain.Action} — Command 패턴 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 저장은 비동기 처리되며, 서버 요청 실패 시에도 로컬 상태는 변경되지 않는다.
 * rollback은 execute 시점의 스냅샷을 기준으로 복원한다.</p>
 */
public class SaveDocumentAction implements Action {
    private final DocumentList documentList;
    private final DocumentRepository documentRepository;
    private final ActionManager actionManager;
    private List<DocumentValue> snapshot;

    public SaveDocumentAction(DocumentList documentList, DocumentRepository documentRepository, ActionManager actionManager) {
        this.documentList = documentList;
        this.documentRepository = documentRepository;
        this.actionManager = actionManager;
    }

    @Override
    public void execute() {
        snapshot = new ArrayList<>(documentList.getValue());
        documentRepository.save(snapshot).subscribe(v -> actionManager.clear());
    }

    @Override
    public void rollback() {
        if (snapshot != null) {
            documentList.next(new ArrayList<>(snapshot));
        }
    }
}
