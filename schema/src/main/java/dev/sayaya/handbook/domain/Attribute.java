package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

/**
 * 속성 정의를 나타내는 공용 도메인 모델.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Attribute {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("order") @JsProperty public int order;
    @JsonProperty("type") @JsProperty public AttributeType type;

    @JsOverlay @JsIgnore
    public static Attribute create(String id, String name, int order, AttributeType type) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Attribute name cannot be blank.");
        if (!name.matches("^[a-zA-Z0-9_-]+$")) throw new IllegalArgumentException("Attribute name can only contain alphanumerics, hyphens, and underscores.");
        
        Attribute attr = new Attribute();
        attr.id = id;
        attr.name = name;
        attr.order = order;
        attr.type = type;
        return attr;
    }
}
