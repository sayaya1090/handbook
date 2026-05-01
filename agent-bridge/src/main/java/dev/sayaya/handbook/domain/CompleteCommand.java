package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 백엔드 agent-protocol 의 동일 클래스에 대응하는 GWT 호환 버전.
 *
 * <p><b>책임:</b> complete 커맨드의 요약(summary)과 실행ID(executionId)를
 * JsInterop 네이티브 타입으로 매핑한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 프로토콜 VO)</li></ul></p>
 * <p><b>주의:</b> 네이티브 JsType이므로 new 로 생성할 수 없고, JSON.parse() 결과를 Js.cast()로 변환하여 사용한다.</p>
 *
 * @see jsinterop.base.Js#cast(Object)
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class CompleteCommand {
    /** 실행 완료 요약 */
    private String summary;
    /** 실행 ID */
    private String executionId;
}
