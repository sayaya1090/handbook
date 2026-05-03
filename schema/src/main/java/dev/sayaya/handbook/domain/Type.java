package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Type {
    @JsonProperty("id") @JsProperty(name = "id") private String id;
    @JsonProperty("version") @JsProperty(name = "version") private String version;
    @JsonProperty("description") @JsProperty(name = "description") private String description;
    @JsonProperty("primitive") @JsProperty(name = "primitive") private boolean primitive;
    @JsonProperty("parent") @JsProperty(name = "parent") private String parent;
    @JsonProperty("effect_date_time") @JsProperty(name = "effect_date_time") private double effectDateTime;
    @JsonProperty("expire_date_time") @JsProperty(name = "expire_date_time") private double expireDateTime;
    @JsonProperty("width") @JsProperty(name = "width") private double width;
    @JsonProperty("height") @JsProperty(name = "height") private double height;
    @JsonProperty("attributes") @JsProperty(name = "attributes") private Attribute[] attributes;

    @JsOverlay @JsIgnore
    public static Type create(String id, String version, double effectDateTime, double expireDateTime) {
        Type type = new Type();
        type.id(id);
        type.version(version);
        type.effectDateTime(effectDateTime);
        type.expireDateTime(expireDateTime);
        return type;
    }

    @JsOverlay @JsIgnore
    public static Type createWithSize(String id, String version, double width, double height) {
        Type type = new Type();
        type.id(id);
        type.version(version);
        type.width(width);
        type.height(height);
        return type;
    }

    @JsOverlay
    public String key() {
        return id() + ":" + version();
    }

    @JsOverlay @JsIgnore
    public Type withAttributes(Attribute[] attributes) {
        Type type = create(id(), version(), effectDateTime(), expireDateTime());
        type.description(description());
        type.primitive(primitive());
        type.parent(parent());
        type.width(width());
        type.height(height());
        type.attributes(attributes);
        return type;
    }
}
