package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class AttributeType {
    @JsonProperty("type") @JsProperty public String type;
    @JsonProperty("referencedType") @JsProperty public String referencedType;
    @JsonProperty("elementType") @JsProperty public AttributeType elementType;

    @JsOverlay @JsIgnore
    public static AttributeType text() {
        AttributeType atv = new AttributeType();
        atv.type = "text";
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
