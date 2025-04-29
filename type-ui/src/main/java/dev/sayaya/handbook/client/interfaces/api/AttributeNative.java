package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Attribute;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class AttributeNative {
    @JsProperty public String id;
    @JsProperty public String version;
    @JsProperty(name = "effect_date_time") public Double effectDateTime;
    @JsProperty(name = "expire_date_time") public Double expireDateTime;
    @JsProperty public String description;
    @JsProperty public boolean primitive;
    @JsProperty public Attribute attributes; // 배열
    @JsProperty public String parent;


}
