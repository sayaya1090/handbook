package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Group {
    private String id;
    private String workspace;
    private String name;
    private String description;
}
