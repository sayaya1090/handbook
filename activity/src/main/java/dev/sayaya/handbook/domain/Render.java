package dev.sayaya.handbook.domain;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface Render {
    boolean onInvoke(HTMLElement frame);
}
