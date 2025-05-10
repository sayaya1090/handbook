package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Type;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class AttributeNative {
    @JsProperty public String name;
    @JsProperty public String description;
    @JsProperty public String type;
    @JsProperty(name = "key_type") public String keyType;
    @JsProperty(name = "value_type") public String valueType;
    @JsProperty public boolean nullable;
    @JsProperty public boolean inherited;

    @JsOverlay @JsIgnore public Attribute toDomain(String id, String version, int i) {
        return Attribute.builder()
                .id(id + "$$$" + version + "$$$" + i)
                .name(name).description(description)
                .type(type).keyType(keyType).valueType(valueType)
                .nullable(nullable).inherited(inherited)
                .build();
    }
    @JsOverlay @JsIgnore public static AttributeNative from(Attribute attribute) {
        AttributeNative nativeAttribute = new AttributeNative();
        nativeAttribute.name = attribute.name();
        nativeAttribute.description = attribute.description();
        nativeAttribute.type = attribute.type();
        nativeAttribute.keyType = attribute.keyType();
        nativeAttribute.valueType = attribute.valueType();
        nativeAttribute.nullable = attribute.nullable();
        nativeAttribute.inherited = attribute.inherited();
        return nativeAttribute;
    }
}
