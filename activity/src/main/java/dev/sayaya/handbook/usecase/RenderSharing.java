package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 렌더링(Render) 함수를 공유하는 브릿지.
 * 
 * <p>window 속성: {@code __handbook_render}</p>
 */
public final class RenderSharing {
    private static final String KEY = "__handbook_render";

    private RenderSharing() {}

    /** shell-ui 측: 자식으로부터 렌더링 요청을 수신할 콜백을 등록한다. */
    public static void register(NextFn observer) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, observer);
    }

    /** 자식 모듈 측: 부모 쉘에 렌더링 함수를 전달한다. */
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
