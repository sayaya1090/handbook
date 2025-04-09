package dev.sayaya.handbook.client.usecase;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;

@JsFunction
public interface ToolFunction {
    @JsOverlay default boolean repeat() { // 반환값: 실행 성공 여부
        exec();
        return true;
    }
    void exec();
}
