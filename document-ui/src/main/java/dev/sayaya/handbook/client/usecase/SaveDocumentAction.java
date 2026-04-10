package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.DocumentValue;

import java.util.ArrayList;
import java.util.List;

/** 문서를 저장소에 저장하는 액션. DocumentRepository를 통해 서버에 PUT 요청을 보낸다. */
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
