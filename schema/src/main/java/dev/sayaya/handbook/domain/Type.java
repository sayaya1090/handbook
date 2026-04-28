package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Type {
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("version") @JsProperty private String version;
    @JsonProperty("width") @JsProperty private double width;
    @JsonProperty("height") @JsProperty private double height;
    @JsonProperty("attributes") @JsProperty private Attribute[] attributes;

    @JsOverlay @JsIgnore
    public static Type create(String id, String version, double width, double height) {
        Type type = new Type();
        type.id = id;
        type.version = version;
        type.width = width;
        type.height = height;
        return type;
    }
}
