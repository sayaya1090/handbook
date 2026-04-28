package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 URI(URL) 정보를 공유하고 내비게이션을 수행하는 브릿지.
 * 
 * <p>window 속성: {@code __handbook_uri}</p>
 */
public final class UriSharing {
    private static final String KEY = "__handbook_uri";

    private UriSharing() {}

    /** shell-ui 측: 자식으로부터 URI 변경 요청을 수신할 콜백을 등록한다. */
    public static void register(NavigateFn observer) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, observer);
    }

    /** 자식 모듈 측: 부모 쉘에 특정 경로로 내비게이션을 요청한다. */
    public static void navigate(String uri) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<NavigateFn>cast(win.get(KEY)).call(uri);
    }

    /** 브릿지가 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        return Js.asPropertyMap(DomGlobal.window).has(KEY);
    }

    @JsFunction
    public interface NavigateFn {
        void call(String uri);
    }
}
