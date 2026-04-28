package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

/**
 * 속성의 데이터 타입을 나타내는 공용 도메인 모델.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
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
