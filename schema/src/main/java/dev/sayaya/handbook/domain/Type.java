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
public final class Type implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("version") @JsProperty private String version;
    @JsonProperty("description") @JsProperty private String description;
    @JsonProperty("primitive") @JsProperty private boolean primitive;
    @JsonProperty("parent") @JsProperty private String parent;
    @JsonProperty("effect_date_time") @JsProperty private double effectDateTime;
    @JsonProperty("expire_date_time") @JsProperty private double expireDateTime;
    @JsonProperty("width") @JsProperty private double width;
    @JsonProperty("height") @JsProperty private double height;
    @JsonProperty("attributes") @JsProperty private Attribute[] attributes;

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
