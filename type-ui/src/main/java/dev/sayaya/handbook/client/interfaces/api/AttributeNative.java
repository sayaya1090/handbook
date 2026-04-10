package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.AttributeValue;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** JSON ↔ AttributeValue 변환. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AttributeNative {
    public String name;
    public int order;
    public String description;
    @JsProperty(name = "attribute_type") public AttributeTypeValue type;
    public boolean nullable;
    public boolean inherited;

    @JsOverlay
    public final AttributeValue toDomain() {
        AttributeValue a = AttributeValue.of(name, order, type);
        a.description = description;
        a.nullable = nullable;
        a.inherited = inherited;
        return a;
    }

    @JsOverlay
    public static AttributeNative fromDomain(AttributeValue attr) {
        AttributeNative n = new AttributeNative();
        n.name = attr.name;
        n.order = attr.order;
        n.description = attr.description;
        n.type = attr.type;
        n.nullable = attr.nullable;
        n.inherited = attr.inherited;
        return n;
    }
}
