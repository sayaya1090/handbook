package dev.sayaya.handbook.client.usecase;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.PresenceTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;
import elemental2.core.Global;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * 워크스페이스 SSE 이벤트를 구독하여 문서 목록을 자동 갱신한다.
 *
 * <p>DOCUMENT_CREATED, DOCUMENT_DELETED 이벤트 수신 시:
 * <ol>
 *   <li>현재 선택된 타입의 문서를 다시 조회한다.</li>
 *   <li>토스트 알림을 표시한다.</li>
 * </ol>
 */
@Singleton
public class DocumentEventHandler {
    private final WorkspaceEventReceiver eventReceiver;
    private final TypeProvider typeProvider;
    private final DocumentRepository documentRepository;
    private final DocumentList documentList;
    private final ToastContainer toastContainer;
    private final PresenceTracker presenceTracker;
    private Labels labels = Labels.empty();

    @Inject
    public DocumentEventHandler(WorkspaceEventReceiver eventReceiver,
                                 TypeProvider typeProvider,
                                 DocumentRepository documentRepository,
                                 DocumentList documentList,
                                 ToastContainer toastContainer,
                                 LabelProvider labelProvider,
                                 PresenceTracker presenceTracker) {
        this.eventReceiver = eventReceiver;
        this.typeProvider = typeProvider;
        this.documentRepository = documentRepository;
        this.documentList = documentList;
        this.toastContainer = toastContainer;
        this.presenceTracker = presenceTracker;
        labelProvider.subscribe(l -> this.labels = l);
    }

    public void init() {
        eventReceiver.events().subscribe(this::handleEvent);
    }

    private void handleEvent(String eventData) {
        if (eventData == null) return;
        int colonIdx = eventData.indexOf(':');
        if (colonIdx < 0) return;
        String eventType = eventData.substring(0, colonIdx);

        switch (eventType) {
            case "DOCUMENT_CREATED":
            case "DOCUMENT_DELETED":
                refreshDocuments();
                toastContainer.show(ToastLevel.INFO, labels.getOrDefault("document.event.changed", "Another user has modified the document"));
                break;
            case "PRESENCE":
                handlePresence(eventData.substring(colonIdx + 1));
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePresence(String json) {
        GWT.log("DocumentEventHandler: presence event: " + json);
        JsPropertyMap<Object> parsed = Js.cast(Global.JSON.parse(json));
        String user = Js.cast(parsed.get("user"));
        String userName = parsed.has("user_name") ? Js.cast(parsed.get("user_name")) : user;
        String type = parsed.has("type") ? Js.cast(parsed.get("type")) : null;
        String serial = parsed.has("serial") ? Js.cast(parsed.get("serial")) : null;
        String field = parsed.has("field") ? Js.cast(parsed.get("field")) : null;
        presenceTracker.update(user, userName, type, serial, field);
    }

    private void refreshDocuments() {
        var type = typeProvider.getValue();
        if (type == null || type.id == null) return;
        GWT.log("DocumentEventHandler: refreshing documents for type " + type.id);
        documentRepository.search(type.id, 0, 50).subscribe(docs -> {
            if (docs != null) {
                List<DocumentValue> docList = Arrays.asList(docs);
                documentList.next(docList);
            }
        });
    }
}
