package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent = true)
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
public final class Label {
    private String addType;
    private String undo;
    private String redo;
}
