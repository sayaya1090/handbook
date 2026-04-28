package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 진행 상태(Progress) 정보를 공유하는 브릿지.
 * 
 * <p>window 속성: {@code __handbook_progress}</p>
 */
public final class ProgressSharing {
    private static final String KEY = "__handbook_progress";

    private ProgressSharing() {}

    /** shell-ui 측: 자식으로부터 진행률 정보를 수신할 콜백을 등록한다. */
    public static void register(NextFn observer) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, observer);
    }

    /** 자식 모듈 측: 부모 쉘에 진행률 정보를 전달한다. */
    public static void next(Object progress) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<NextFn>cast(win.get(KEY)).call(progress);
    }

    @JsFunction
    public interface NextFn {
        void call(Object value);
    }
}
