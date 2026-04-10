package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * 타입 스키마. backend Type의 GWT 호환 표현.
 * 캔버스 위치(x/y/w/h)는 포함하지 않는다 — PositionMap에서 별도 관리.
 * 변경 상태(NOT_CHANGE/CHANGE/DELETE)도 포함하지 않는다 — ChangeTracker에서 별도 관리.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class TypeValue {
    public String id;
    public String version;
    @JsProperty(name = "effect_date_time")
    public double effectDateTime;
    @JsProperty(name = "expire_date_time")
    public double expireDateTime;
    public String description;
    public boolean primitive;
    public String parent;
    public AttributeValue[] attributes;

    @JsOverlay
    public static TypeValue create(String id, String version, double effectDateTime, double expireDateTime) {
        TypeValue t = new TypeValue();
        t.id = id;
        t.version = version;
        t.effectDateTime = effectDateTime;
        t.expireDateTime = expireDateTime;
        t.primitive = false;
        t.attributes = new AttributeValue[0];
        return t;
    }

    @JsOverlay
    public final String key() {
        return id + ":" + version;
    }

    @JsOverlay
    public final TypeValue withAttributes(AttributeValue[] attributes) {
        TypeValue t = copy();
        t.attributes = attributes;
        return t;
    }

    @JsOverlay
    public final TypeValue withDescription(String description) {
        TypeValue t = copy();
        t.description = description;
        return t;
    }

    @JsOverlay
    public final TypeValue withParent(String parent) {
        TypeValue t = copy();
        t.parent = parent;
        return t;
    }

    @JsOverlay
    private TypeValue copy() {
        TypeValue t = new TypeValue();
        t.id = this.id;
        t.version = this.version;
        t.effectDateTime = this.effectDateTime;
        t.expireDateTime = this.expireDateTime;
        t.description = this.description;
        t.primitive = this.primitive;
        t.parent = this.parent;
        t.attributes = this.attributes;
        return t;
    }
}
