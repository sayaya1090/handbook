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
 * <p><b>책임:</b> navigate 커맨드의 메뉴(menu), 도구(tool), URL 필드를 JsInterop 네이티브 타입으로 매핑한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 프로토콜 VO)</li></ul></p>
 * <p><b>주의:</b> 네이티브 JsType이므로 new 로 생성할 수 없고, JSON.parse() 결과를 Js.cast()로 변환하여 사용한다.</p>
 *
 * @see jsinterop.base.Js#cast(Object)
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class NavigateCommand {
    /** 이동할 메뉴 이름 */
    private String menu;
    /** 이동할 도구 이름 */
    private String tool;
    /** 이동할 URL */
    private String url;
}
