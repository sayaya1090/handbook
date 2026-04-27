package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

/**
 * window CustomEvent를 통해 GWT 모듈 간 워크스페이스 SSE 이벤트를 전달하는 브릿지.
 *
 * <p>shell-ui는 {@link #publish(String, String)}로 이벤트를 발행하고,
 * document-ui/type-ui는 {@link #receiver()}로 WorkspaceEventReceiver를 가져와 구독한다.
 *
 * <p>이벤트 이름: {@code handbook-workspace-event}
 * <p>detail 형식: "EVENT_TYPE:payload_json"
 */
public final class WindowWorkspaceEventBridge {
    private static final String EVENT_NAME = "handbook-workspace-event";
    private static final String CONTEXT_EVENT_NAME = "handbook-workspace-context";
    private static final BehaviorSubject<String> subject = BehaviorSubject.behavior(null);
    private static final BehaviorSubject<String> workspaceIdSubject = BehaviorSubject.behavior(null);
    private static boolean listenerRegistered = false;

    private WindowWorkspaceEventBridge() {}

    /**
     * shell-ui 측: 워크스페이스 SSE 이벤트를 window CustomEvent로 발행한다.
     *
     * @param eventType 이벤트 타입 (예: DOCUMENT_CREATED, TYPE_DELETED)
     * @param payload   이벤트 페이로드 JSON 문자열
     */
    public static void publish(String eventType, String payload) {
        String detail = eventType + ":" + (payload != null ? payload : "");
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(detail);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(EVENT_NAME, init));
    }

    /**
     * shell-ui 측: 현재 선택된 워크스페이스 ID가 변경되었음을 브라우저 전체에 발행한다.
     */
    public static void publishWorkspace(String workspaceId) {
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(workspaceId);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(CONTEXT_EVENT_NAME, init));
    }

    /** 편집 모듈 측: WorkspaceEventReceiver를 반환한다. */
    public static WorkspaceEventReceiver receiver() {
        ensureListener();
        return new WorkspaceEventReceiver() {
            @Override
            public dev.sayaya.rx.Observable<String> events() {
                return subject.asObservable();
            }

            @Override
            public dev.sayaya.rx.Observable<String> workspaceId() {
                return workspaceIdSubject.asObservable();
            }
        };
    }

    private static void ensureListener() {
        if (listenerRegistered) return;
        listenerRegistered = true;
        DomGlobal.window.addEventListener(EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail == null) return;
            String data = Js.cast(detail);
            if (data.isEmpty()) return;
            subject.next(data);
        });
        DomGlobal.window.addEventListener(CONTEXT_EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail == null) return;
            String data = Js.cast(detail);
            workspaceIdSubject.next(data);
        });
    }
}
