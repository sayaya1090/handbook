package dev.sayaya.handbook.client.usecase;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface Render {
    boolean onInvoke(HTMLElement frame);    // 반환값: 렌더링 처리 여부
}
