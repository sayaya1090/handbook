package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.usecase.MutationReceiver;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/** MutationReceiver를 구독하여 에이전트 명령을 Action으로 변환한다. */
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
