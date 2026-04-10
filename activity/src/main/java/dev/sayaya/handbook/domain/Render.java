package dev.sayaya.handbook.domain;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;

/**
 * 프레임 요소에 콘텐츠를 렌더링하는 콜백 함수 인터페이스.
 *
 * <p><b>책임:</b> JsFunction으로 JavaScript 호출 가능한 렌더링 콜백을 정의한다. HTMLElement를 받아 렌더링 성공 여부를 반환한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 함수형 인터페이스)</li></ul></p>
 */
@JsFunction
public interface Render {
    boolean onInvoke(HTMLElement frame);
}
