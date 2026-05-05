package dev.sayaya.handbook.client.usecase;

import com.google.gwt.core.client.GWT;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.EventSource;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 워크스페이스 SSE 클라이언트.
 * URI 변경을 감시하여 현재 워크스페이스 ID를 추출하고,
 * /workspaces/{id}/messages 엔드포인트에 SSE 연결을 맺어
 * 수신된 도메인 이벤트를 window CustomEvent로 전파한다.
 *
 * <p>이벤트 이름: {@code handbook-workspace-event}
 * <p>detail 형식: "EVENT_TYPE:payload_json"
 */
@Singleton
public class WorkspaceEventListener {
    private static final String BRIDGE_EVENT_NAME = "handbook-workspace-event";
    private final BehaviorSubject<String> uri;
    private final SessionContext sessionContext;
    private EventSource eventSource;
    private String currentWorkspaceId;

    @Inject
    WorkspaceEventListener(UriStore uri, SessionContext sessionContext) {
        this.uri = uri;
        this.sessionContext = sessionContext;
    }

    public void initialize() {
        uri.subscribe(this::onUriChanged);
    }

    private void onUriChanged(String newUri) {
        String wsId = extractWorkspaceId(newUri);
        sessionContext.set("workspaceId", wsId);
        if (wsId == null) {
            disconnect();
            currentWorkspaceId = null;
            return;
        }
        if (wsId.equals(currentWorkspaceId)) return;
        currentWorkspaceId = wsId;
        connect(wsId);
    }

    private void connect(String wsId) {
        disconnect();
        GWT.log("WorkspaceEventListener: connecting to /workspaces/" + wsId + "/messages");
        eventSource = new EventSource("/workspaces/" + wsId + "/messages");
        // SSE 이벤트 타입별 리스너 등록
        addTypedListener("DOCUMENT_CREATED");
        addTypedListener("DOCUMENT_DELETED");
        addTypedListener("TYPE_CREATED");
        addTypedListener("TYPE_DELETED");
        addTypedListener("VALIDATION_REQUESTED");
        addTypedListener("AGENT_COMMAND");
        eventSource.onerror = event -> {
            GWT.log("WorkspaceEventListener: SSE connection error, will auto-reconnect");
        };
    }

    private void addTypedListener(String eventType) {
        eventSource.addEventListener(eventType, event -> {
            String data = Js.cast(Js.asPropertyMap(event).get("data"));
            if (data == null) return;
            dispatch(eventType, data);
        });
    }

    private void dispatch(String eventType, String payload) {
        String detail = eventType + ":" + (payload != null ? payload : "");
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(detail);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(BRIDGE_EVENT_NAME, init));
    }

    private void disconnect() {
        if (eventSource != null) {
            eventSource.close();
            eventSource = null;
        }
    }

    /**
     * URL에서 워크스페이스 ID를 추출한다.
     * 예: "/workspaces/abc-123/type" -> "abc-123"
     */
    public static String extractWorkspaceId(String url) {
        if (url == null) return null;
        int idx = url.indexOf("/workspaces/");
        if (idx < 0) return null;
        String rest = url.substring(idx + "/workspaces/".length());
        int slashIdx = rest.indexOf('/');
        String wsId = slashIdx >= 0 ? rest.substring(0, slashIdx) : rest;
        // 쿼리스트링 제거
        int queryIdx = wsId.indexOf('?');
        if (queryIdx >= 0) wsId = wsId.substring(0, queryIdx);
        // 온보딩 경로는 워크스페이스 ID가 아님
        if ("onboarding".equals(wsId)) return null;
        return wsId.isEmpty() ? null : wsId;
    }
}
