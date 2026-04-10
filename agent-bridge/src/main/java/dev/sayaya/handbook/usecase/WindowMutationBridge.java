package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.core.JsArray;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

/**
 * window CustomEvent를 통해 GWT 모듈 간 MutationReceiver를 연결하는 브릿지.
 *
 * <p>agent-ui는 {@link #publish(String[])}로 mutations을 발행하고,
 * type-ui/workspace-ui는 {@link #receiver()}로 MutationReceiver를 가져와 구독한다.
 *
 * <p>이벤트 이름: {@code handbook-mutate}
 */
public final class WindowMutationBridge {
    private static final String EVENT_NAME = "handbook-mutate";
    private static final BehaviorSubject<String[]> subject = BehaviorSubject.behavior(null);
    private static boolean listenerRegistered = false;

    private WindowMutationBridge() {}

    /** 에이전트 측: mutations을 window CustomEvent로 발행한다. */
    public static void publish(String[] changes) {
        JsArray<String> detail = new JsArray<>();
        if (changes != null) {
            for (String change : changes) detail.push(change);
        }
        @SuppressWarnings("unchecked")
        CustomEventInit<JsArray<String>> init = Js.cast(CustomEventInit.create());
        init.setDetail(detail);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(EVENT_NAME, init));
    }

    /** 편집 모듈 측: MutationReceiver를 반환한다. window 이벤트를 구독하여 subject로 전달. */
    public static MutationReceiver receiver() {
        ensureListener();
        return () -> subject.asObservable();
    }

    private static void ensureListener() {
        if (listenerRegistered) return;
        listenerRegistered = true;
        DomGlobal.window.addEventListener(EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail == null) return;
            JsArray<String> arr = Js.cast(detail);
            if (arr.length == 0) return;
            String[] result = new String[arr.length];
            for (int i = 0; i < arr.length; i++) result[i] = arr.getAt(i);
            subject.next(result);
        });
    }
}
