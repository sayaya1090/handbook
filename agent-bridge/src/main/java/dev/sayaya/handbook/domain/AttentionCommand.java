package dev.sayaya.handbook.domain;

import jsinterop.annotations.*;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 백엔드 agent-protocol 의 동일 클래스에 대응하는 GWT 호환 버전.
 *
 * <p><b>책임:</b> attention 커맨드의 대상(target), 스타일(style), 메시지(message), 위치(position),
 * 해제 가능 여부(dismissable)를 JsInterop 네이티브 타입으로 매핑한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 프로토콜 VO). style 문자열을 {@link AttentionStyle}로 변환하는 것은 소비자 책임.</li></ul></p>
 * <p><b>주의:</b> 네이티브 JsType이므로 new 로 생성할 수 없고, JSON.parse() 결과를 Js.cast()로 변환하여 사용한다.
 * style 필드는 JSON 원본 문자열을 그대로 보유하며, 열거형 변환은 핸들러 측에서 수행한다.</p>
 *
 * @see AttentionStyle
 * @see jsinterop.base.Js#cast(Object)
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class AttentionCommand {
    /** 오버레이 대상의 CSS 셀렉터 */
    private String target;
    /** 오버레이 스타일 (COACHMARK, SPOTLIGHT, PULSE, ARROW, BADGE). 문자열로 보유. */
    private String style;
    /** 오버레이에 표시할 메시지 */
    private String message;
    /** 오버레이 위치 (top, bottom, left, right) */
    private String position;
    /** 사용자 해제 가능 여부 */
    private boolean dismissable;
}
