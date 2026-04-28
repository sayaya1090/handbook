package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Attribute {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("order") @JsProperty public int order;
    @JsonProperty("type") @JsProperty public AttributeType type;

    @JsOverlay @JsIgnore
    public static Attribute create(String id, String name, int order, AttributeType type) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Attribute name cannot be blank.");
        Attribute attr = new Attribute();
        attr.id = id;
        attr.name = name;
        attr.order = order;
        attr.type = type;
        return attr;
    }
}
