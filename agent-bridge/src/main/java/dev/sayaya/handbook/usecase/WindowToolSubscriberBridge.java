package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * 부모 쉘(Host)에서 자식 프레임(Child)으로 선택된 도구 이벤트를 전달하기 위한 브릿지.
 * 
 * <p>window 속성: {@code __handbook_tool_subscriber}</p>
 */
public final class WindowToolSubscriberBridge {
    private static final String KEY = "__handbook_tool_subscriber";

    private WindowToolSubscriberBridge() {}

    /** 자식(Child) 측: 쉘로부터 도구 선택 이벤트를 수신할 콜백을 등록한다. */
    public static void register(SubscriberFn callback) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, callback);
    }

    /** 쉘(Host) 측: 선택된 도구 ID를 자식 프레임에 알린다. */
    public static void select(String toolId) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<SubscriberFn>cast(win.get(KEY)).call(toolId);
    }

    @JsFunction
    public interface SubscriberFn {
        void call(String toolId);
    }
}
