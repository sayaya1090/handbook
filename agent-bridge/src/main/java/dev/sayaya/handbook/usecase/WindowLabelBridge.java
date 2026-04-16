package dev.sayaya.handbook.usecase;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 다국어 레이블을 공유하는 브릿지.
 *
 * <p>shell-ui 는 {@link #publish(Object)}로 최신 Labels 를 window 에 게시하고,
 * agent-ui 는 {@link #snapshot()}으로 현재 Labels 를 읽거나
 * {@link #EVENT_NAME} CustomEvent 를 구독해 변경을 감지한다.</p>
 *
 * <p>Labels 는 {@code @JsType(isNative=true, name="Object")} 이므로 plain JS 객체로 모듈 간 전달 가능.</p>
 *
 * <p>window 속성: {@code __handbook_labels}<br>
 * CustomEvent: {@code handbook-labels-changed}
 */
public final class WindowLabelBridge {
    private static final String KEY = "__handbook_labels";
    public static final String EVENT_NAME = "handbook-labels-changed";

    private WindowLabelBridge() {}

    /** shell-ui 측: Labels 객체를 window 에 게시하고 변경 이벤트를 발행한다. */
    public static void publish(Object labels) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, labels);
        @SuppressWarnings("unchecked")
        CustomEventInit<Object> init = Js.cast(CustomEventInit.create());
        init.setDetail(labels);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(EVENT_NAME, init));
    }

    /** agent-ui 측: 현재 게시된 Labels 를 읽는다. */
    public static Object snapshot() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        return win.has(KEY) ? win.get(KEY) : null;
    }

    /** 브릿지가 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        return Js.asPropertyMap(DomGlobal.window).has(KEY);
    }
}
