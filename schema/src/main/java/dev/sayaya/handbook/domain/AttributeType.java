package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class AttributeType implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("type") @JsProperty private String type;
    @JsonProperty("referencedType") @JsProperty public String referencedType;
    @JsonProperty("elementType") @JsProperty public AttributeType elementType;
    @JsonProperty("allowedValues") @JsProperty public String[] allowedValues;

    @JsOverlay @JsIgnore
    public static AttributeType text() {
        AttributeType atv = new AttributeType();
        atv.type = "text";
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType enumType(String[] values) {
        AttributeType atv = new AttributeType();
        atv.type = "enum";
        atv.allowedValues = values;
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType number() {
        AttributeType atv = new AttributeType();
        atv.type = "number";
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType date() {
        AttributeType atv = new AttributeType();
        atv.type = "date";
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType bool() {
        AttributeType atv = new AttributeType();
        atv.type = "bool";
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType document(String referencedType) {
        AttributeType atv = new AttributeType();
        atv.type = "document";
        atv.referencedType = referencedType;
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType array(AttributeType element) {
        AttributeType atv = new AttributeType();
        atv.type = "array";
        atv.elementType = element;
        return atv;
    }

    @JsOverlay
    public final String simplify() {
        if ("array".equals(type) && elementType != null) {
            return elementType.simplify() + "[]";
        }
        if ("document".equals(type)) {
            return referencedType != null ? referencedType : "document";
        }
        return type != null ? type : "unknown";
    }
}
