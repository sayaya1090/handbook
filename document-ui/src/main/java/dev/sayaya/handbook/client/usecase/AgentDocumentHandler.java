package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.usecase.MutationReceiver;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 에이전트의 문서 조작 명령을 처리하는 핸들러.
 *
 * <p><b>책임:</b> {@link dev.sayaya.handbook.usecase.MutationReceiver}를 구독하여 에이전트로부터
 * 수신된 문자열 명령(DOC_SELECT, DOC_ADD, DOC_EDIT, DOC_DELETE, DOC_SAVE)을
 * 대응하는 {@link dev.sayaya.handbook.domain.Action}으로 변환하고 실행한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeProvider} — DOC_SELECT 시 타입 선택 변경</li>
 *   <li>{@link TypeList} — 타입 이름으로 TypeInfo 조회</li>
 *   <li>{@link dev.sayaya.handbook.client.components.ActionManager} — 액션 실행 및 Undo/Redo 관리</li>
 *   <li>{@link DocumentList} — 문서 목록 상태 조회/갱신</li>
 *   <li>{@link dev.sayaya.handbook.usecase.MutationReceiver} — 에이전트 명령 수신 채널</li>
 *   <li>{@link DocumentRepository} — DOC_SAVE 시 서버 저장</li>
 * </ul></p>
 *
 * <p><b>주의:</b> init()을 호출해야 구독이 시작된다. DOC_EDIT 명령 형식은
 * "DOC_EDIT &lt;serial&gt; &lt;field&gt; &lt;value&gt;"이며, 인식되지 않는 명령은 무시한다.</p>
 */
@Singleton
public class AgentDocumentHandler {
    private final TypeProvider typeProvider;
    private final TypeList typeList;
    private final ActionManager actionManager;
    private final DocumentList documentList;
    private final MutationReceiver mutationReceiver;
    private final DocumentRepository documentRepository;

    @Inject
    public AgentDocumentHandler(TypeProvider typeProvider, TypeList typeList,
                                 ActionManager actionManager, DocumentList documentList,
                                 MutationReceiver mutationReceiver, DocumentRepository documentRepository) {
        this.typeProvider = typeProvider;
        this.typeList = typeList;
        this.actionManager = actionManager;
        this.documentList = documentList;
        this.mutationReceiver = mutationReceiver;
        this.documentRepository = documentRepository;
    }

    public void init() {
        mutationReceiver.mutations().subscribe(this::processChanges);
    }

    private void processChanges(String[] changes) {
        if (changes == null) return;
        for (String change : changes) {
            processChange(change);
        }
    }

    private void processChange(String change) {
        if (change == null || change.isEmpty()) return;
        String[] parts = change.split(" ", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "DOC_SELECT" -> handleSelect(args.trim());
            case "DOC_ADD" -> handleAdd();
            case "DOC_EDIT" -> handleEdit(args.trim());
            case "DOC_DELETE" -> handleDelete(args.trim());
            case "DOC_SAVE" -> handleSave();
            default -> { /* 무시 — 다른 모듈의 명령일 수 있음 */ }
        }
    }

    private void handleSelect(String typeName) {
        var types = typeList.getValue();
        for (var type : types) {
            if (type.id != null && type.id.equals(typeName)) {
                typeProvider.next(type);
                return;
            }
        }
    }

    private void handleAdd() {
        DocumentValue newDoc = new DocumentValue();
        newDoc.data = JsPropertyMap.of();
        actionManager.execute(new AddDocumentAction(documentList, newDoc));
    }

    private void handleEdit(String args) {
        // 형식: <serial> <field> <value>
        String[] parts = args.split(" ", 3);
        if (parts.length < 3) return;
        String serial = parts[0];
        String field = parts[1];
        String value = parts[2];

        List<DocumentValue> docs = documentList.getValue();
        for (int i = 0; i < docs.size(); i++) {
            DocumentValue doc = docs.get(i);
            if (doc.serial != null && doc.serial.equals(serial)) {
                String before = doc.data != null ? (String) doc.data.get(field) : null;
                actionManager.execute(new EditDocumentAction(documentList, i, field, before, value));
                return;
            }
        }
    }

    private void handleDelete(String serial) {
        List<DocumentValue> docs = documentList.getValue();
        List<DocumentValue> toDelete = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            if (docs.get(i).serial != null && docs.get(i).serial.equals(serial)) {
                toDelete.add(docs.get(i));
                indices.add(i);
            }
        }
        if (!toDelete.isEmpty()) {
            actionManager.execute(new DeleteDocumentAction(documentList, toDelete, indices));
        }
    }

    private void handleSave() {
        List<DocumentValue> docs = documentList.getValue();
        documentRepository.save(docs).subscribe(v -> actionManager.clear());
    }
}
