package dev.sayaya.handbook.client.usecase;

import elemental2.dom.MouseEvent;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface ToolFunction {
    void onClick(MouseEvent evt);
}
