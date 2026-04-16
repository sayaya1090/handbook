package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 URI Observer 를 연결하는 브릿지.
 *
 * <p>shell-ui 는 {@link #register(NavigateFn)}으로 자신의 Observer&lt;String&gt;.next 를 등록하고,
 * agent-ui 는 {@link #navigate(String)}으로 URL 을 전달한다.</p>
 *
 * <p>window 속성: {@code __handbook_uri}
 */
public final class WindowUriBridge {
    private static final String KEY = "__handbook_uri";

    private WindowUriBridge() {}

    /** shell-ui 측: Observer&lt;String&gt;.next 를 window 에 등록한다. */
    public static void register(NavigateFn observer) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, observer);
    }

    /** agent-ui 측: URL 을 shell 의 Observer 에 전달한다. */
    public static void navigate(String url) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<NavigateFn>cast(win.get(KEY)).call(url);
    }

    /** 브릿지가 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        return Js.asPropertyMap(DomGlobal.window).has(KEY);
    }

    @JsFunction
    public interface NavigateFn {
        void call(String url);
    }
}
