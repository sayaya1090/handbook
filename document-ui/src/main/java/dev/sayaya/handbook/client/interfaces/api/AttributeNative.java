package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Attribute;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class AttributeNative {
    @JsProperty public String name;
    @JsProperty public String description;
    @JsProperty public double order;
    @JsProperty public AttributeTypeDefinitionNative type;
    @JsProperty public boolean nullable;
    @JsProperty public boolean inherited;

    @JsOverlay @JsIgnore public Attribute toDomain(String id, String version, int i) {
        return Attribute.builder()
                .id(id + "$$$" + version + "$$$" + i)
                .name(name).description(description)
                .type(type!=null?type.toDomain():null)
                .nullable(nullable).inherited(inherited)
                .build();
    }
}
