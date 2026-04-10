package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 StateProvider를 연결하는 브릿지.
 *
 * <p>type-ui는 {@link #register(StateProvider)}로 자신의 StateProvider를 등록하고,
 * agent-ui는 {@link #snapshot()}으로 현재 상태를 조회한다.
 *
 * <p>window 속성: {@code __handbook_stateProvider}
 */
public final class WindowStateProviderBridge {
    private static final String KEY = "__handbook_stateProvider";

    private WindowStateProviderBridge() {}

    /** 편집 모듈 측: StateProvider를 window에 등록한다. */
    public static void register(StateProvider provider) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        win.set(KEY, (StateProviderFn) () -> provider.snapshot());
    }

    /** 에이전트 측: 등록된 StateProvider의 snapshot을 호출한다. */
    public static String snapshot() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        Object fn = win.get(KEY);
        if (fn == null) return null;
        return Js.<StateProviderFn>cast(fn).call();
    }

    /** StateProvider가 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        return win.has(KEY);
    }

    @jsinterop.annotations.JsFunction
    private interface StateProviderFn {
        String call();
    }
}
