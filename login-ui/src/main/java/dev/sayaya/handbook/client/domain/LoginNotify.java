package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * notify 커맨드 VO — 백엔드 NotifyCommand 에 대응.
 * plain JS 객체로 모듈 간 전달 가능.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class LoginNotify {
    private String level;
    private String message;
}
