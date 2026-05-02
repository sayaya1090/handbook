package dev.sayaya.handbook.client.usecase;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.PresenceTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Document;
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
import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;

/**
 * 워크스페이스 SSE 이벤트를 구독하여 문서 관련 이벤트를 처리한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>DOCUMENT_CREATED/DELETED — 현재 타입의 문서 목록 재조회 + 토스트 표시</li>
 *   <li>PRESENCE — JSON 파싱 후 {@link PresenceTracker}에 편집 위치 전달</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link WorkspaceEventReceiver} — SSE 이벤트 스트림 (shell-ui에서 CustomEvent로 브릿지)</li>
 *   <li>{@link DocumentApi} — 문서 재조회 (PATCH/PUT 지원)</li>
 *   <li>{@link PresenceTracker} — 프레즌스 상태 관리 (ui-components)</li>
 *   <li>{@link LabelProvider} — 토스트 메시지 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 이벤트 문자열은 "EVENT_TYPE:json_payload" 형식. 콜론 기준으로 분리한다.
 * {@link #init()}을 Application에서 호출해야 구독이 시작된다.</p>
 */
@Singleton
public class DocumentEventHandler {
    private final WorkspaceEventReceiver eventReceiver;
    private final TypeProvider typeProvider;
    private final DocumentApi documentApi;
    private final DocumentList documentList;
    private final ToastContainer toastContainer;
    private final PresenceTracker presenceTracker;
    private Labels labels = Labels.empty();

    @Inject
    public DocumentEventHandler(WorkspaceEventReceiver eventReceiver,
                                 TypeProvider typeProvider,
                                 DocumentApi documentApi,
                                 DocumentList documentList,
                                 ToastContainer toastContainer,
                                 LabelProvider labelProvider,
                                 PresenceTracker presenceTracker) {
        this.eventReceiver = eventReceiver;
        this.typeProvider = typeProvider;
        this.documentApi = documentApi;
        this.documentList = documentList;
        this.toastContainer = toastContainer;
        this.presenceTracker = presenceTracker;
        labelProvider.subscribe(l -> this.labels = l);
    }

    public void init() {
        eventReceiver.events().subscribe(this::handleEvent);
        
        // 워크스페이스 변경 이벤트 수신 (agent-bridge 계약 준수)
        DomGlobal.window.addEventListener("handbook-workspace-context", evt -> {
            CustomEvent<String> ce = Js.cast(evt);
            if (ce.detail != null) {
                documentApi.setWorkspace(ce.detail);
                refreshDocuments();
            }
        });
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
        if (type == null || type.id() == null) return;
        
        // 1. 현재 로컬에만 존재하는(저장되지 않은) 신규 행들을 추출 (ID가 없는 경우)
        List<Document> localNewDocs = new java.util.ArrayList<>();
        List<Document> currentDocs = documentList.getValue();
        if (currentDocs != null) {
            for (Document doc : currentDocs) {
                if (doc.id() == null) localNewDocs.add(doc);
            }
        }

        documentApi.search(type.id(), 0, 50).subscribe(serverDocs -> {
            if (serverDocs != null) {
                List<Document> mergedList = new java.util.ArrayList<>(java.util.Arrays.asList(serverDocs));
                // 2. 서버 데이터와 중복되지 않는 로컬 신규 행들만 병합
                for (Document local : localNewDocs) {
                    boolean duplicate = false;
                    for (Document server : serverDocs) {
                        if (local.serial() != null && local.serial().equals(server.serial())) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) mergedList.add(local);
                }
                documentList.next(mergedList);
            }
        });
    }
}
