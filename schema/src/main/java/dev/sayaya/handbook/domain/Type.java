package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

/**
 * 타입 스키마를 표현하는 공용 도메인 모델.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Type {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("version") @JsProperty public String version;
    @JsonProperty("width") @JsProperty public double width;
    @JsonProperty("height") @JsProperty public double height;
    @JsonProperty("attributes") @JsProperty public Attribute[] attributes;

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
