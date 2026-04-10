package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;

/**
 * 도구 클릭 시 실행되는 콜백 함수 인터페이스.
 *
 * <p><b>책임:</b> JsFunction으로 JavaScript 호출 가능한 도구 실행 콜백을 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 함수형 인터페이스)</li></ul></p>
 * <p><b>주의:</b> repeat() 기본 메서드는 exec() 실행 후 항상 true를 반환한다.</p>
 */
@JsFunction
public interface ToolFunction {
    @JsOverlay default boolean repeat() { // 반환값: 실행 성공 여부
        exec();
        return true;
    }
    void exec();
}
