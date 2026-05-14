package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

/**
 * window CustomEvent를 통해 GWT 모듈 간 워크스페이스 이벤트(SSE 등)를 공유하는 브릿지.
 */
public final class WorkspaceEvent {
    private static final String ID_EVENT = "handbook-workspace-context";
    private static final String DOMAIN_EVENT = "handbook-workspace-event";
    private static final BehaviorSubject<String> workspaceIdSubject = BehaviorSubject.behavior("");
    private static final BehaviorSubject<String> eventSubject = BehaviorSubject.behavior("");
    private static boolean listenerRegistered = false;

    private WorkspaceEvent() {}

    /** shell-ui 측: 현재 워크스페이스 ID를 공유한다. */
    public static void publishId(String workspaceId) {
        jsinterop.base.Js.asPropertyMap(DomGlobal.window).set("__handbook_workspace_id__", workspaceId);
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(workspaceId);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(ID_EVENT, init));
        workspaceIdSubject.next(workspaceId);
    }

    /** shell-ui 측: 수신된 도메인 이벤트를 공유한다. */
    public static void publishEvent(String event) {
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(event);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(DOMAIN_EVENT, init));
        eventSubject.next(event);
    }

    /** 각 모듈 측: 워크스페이스 이벤트 수신기를 반환한다. */
    public static WorkspaceEventReceiver receiver() {
        ensureListener();
        return new WorkspaceEventReceiver() {
            @Override public Observable<String> events() { return eventSubject.asObservable(); }
            @Override public Observable<String> workspaceId() { return workspaceIdSubject.asObservable(); }
            @Override public String currentWorkspaceId() { 
                Object globalVal = jsinterop.base.Js.asPropertyMap(DomGlobal.window).get("__handbook_workspace_id__");
                if (globalVal != null && globalVal instanceof String) return (String) globalVal;
                return workspaceIdSubject.getValue(); 
            }
        };
    }

    private static void ensureListener() {
        if (listenerRegistered) return;
        listenerRegistered = true;
        DomGlobal.window.addEventListener(ID_EVENT, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail != null) workspaceIdSubject.next(Js.cast(detail));
        });
        DomGlobal.window.addEventListener(DOMAIN_EVENT, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail != null) eventSubject.next(Js.cast(detail));
        });
    }
}
