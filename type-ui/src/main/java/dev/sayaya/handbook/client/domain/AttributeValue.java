package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 타입의 개별 속성. backend Attribute의 GWT 호환 표현. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class AttributeValue {
    public String name;
    public int order;
    public String description;
    public AttributeTypeValue type;
    public boolean nullable;
    public boolean inherited;

    @JsOverlay
    public static AttributeValue of(String name, int order, AttributeTypeValue type) {
        AttributeValue a = new AttributeValue();
        a.name = name;
        a.order = order;
        a.type = type;
        a.nullable = false;
        a.inherited = false;
        return a;
    }

    @JsOverlay
    public final AttributeValue withName(String name) {
        AttributeValue a = copy();
        a.name = name;
        return a;
    }

    @JsOverlay
    public final AttributeValue withType(AttributeTypeValue type) {
        AttributeValue a = copy();
        a.type = type;
        return a;
    }

    @JsOverlay
    public final AttributeValue withNullable(boolean nullable) {
        AttributeValue a = copy();
        a.nullable = nullable;
        return a;
    }

    @JsOverlay
    public final AttributeValue withDescription(String description) {
        AttributeValue a = copy();
        a.description = description;
        return a;
    }

    @JsOverlay
    private AttributeValue copy() {
        AttributeValue a = new AttributeValue();
        a.name = this.name;
        a.order = this.order;
        a.description = this.description;
        a.type = this.type;
        a.nullable = this.nullable;
        a.inherited = this.inherited;
        return a;
    }
}
