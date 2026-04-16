package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 Render Observer 를 연결하는 브릿지.
 *
 * <p>shell-ui 는 {@link #register(NextFn)}으로 자신의 Observer&lt;Render&gt;.next 를 등록하고,
 * 자식 모듈(login-ui, document-ui 등)은 {@link #next(Object)}로 Render 함수를 전달한다.
 * Render 는 {@code @JsType(isNative=true, name="Object")} 이므로 plain JS 객체로 모듈 간 전달 가능.</p>
 *
 * <p>window 속성: {@code __handbook_render}
 */
public final class WindowRenderBridge {
    private static final String KEY = "__handbook_render";

    private WindowRenderBridge() {}

    /** shell-ui 측: Observer&lt;Render&gt;.next 를 window 에 등록한다. */
    public static void register(NextFn observer) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, observer);
    }

    /** 자식 모듈 측: Render 객체를 shell 의 Observer 에 전달한다. */
    public static void next(Object render) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<NextFn>cast(win.get(KEY)).call(render);
    }

    /** 브릿지가 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        return Js.asPropertyMap(DomGlobal.window).has(KEY);
    }

    @JsFunction
    public interface NextFn {
        void call(Object value);
    }
}
