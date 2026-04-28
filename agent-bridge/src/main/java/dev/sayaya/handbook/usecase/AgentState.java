package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 에이전트의 상태 조회 기능을 공유하는 브릿지.
 */
public final class AgentState {
    private static final String KEY = "__handbook_state_provider";

    private AgentState() {}

    /** 각 모듈 측: 자신의 상태 공급자(StateProvider)를 등록한다. */
    public static void register(StateProvider provider) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, provider);
    }

    /** 에이전트 측: 현재 등록된 상태 공급자를 가져온다. */
    public static StateProvider get() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return null;
        return Js.cast(win.get(KEY));
    }
}
