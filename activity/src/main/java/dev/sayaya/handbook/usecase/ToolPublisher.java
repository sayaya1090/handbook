package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * 자식 프레임(Child)에서 부모 쉘(Host)로 도구 목록을 발행하기 위한 브릿지.
 * 
 * <p>window 속성: {@code __handbook_tool_publisher}</p>
 */
public final class ToolPublisher {
    private static final String KEY = "__handbook_tool_publisher";

    private ToolPublisher() {}

    /** 쉘(Host) 측: 자식으로부터 도구 목록을 수신할 콜백을 등록한다. */
    public static void register(PublisherFn callback) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, callback);
    }

    /** 자식(Child) 측: 쉘에 도구 목록을 전달한다. */
    public static void publish(Object[] tools) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (!win.has(KEY)) return;
        Js.<PublisherFn>cast(win.get(KEY)).call(tools);
    }

    @JsFunction
    public interface PublisherFn {
        void call(Object[] tools);
    }
}
