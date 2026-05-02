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
public final class Attribute {
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("name") @JsProperty private String name;
    @JsonProperty("order") @JsProperty private int order;
    @JsonProperty("type") @JsProperty private AttributeType type;
    @JsonProperty("description") @JsProperty private String description;
    @JsonProperty("nullable") @JsProperty private boolean nullable;
    @JsonProperty("inherited") @JsProperty private boolean inherited;
    @JsonProperty("readRoles") @JsProperty private String[] readRoles;
    @JsonProperty("writeRoles") @JsProperty private String[] writeRoles;

    @JsOverlay @JsIgnore
    public static Attribute create(String id, String name, int order, AttributeType type) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Attribute name cannot be blank.");
        if (!name.matches("^[a-zA-Z0-9_-]+$")) throw new IllegalArgumentException("Attribute name can only contain alphanumerics, hyphens, and underscores.");
        
        Attribute attr = new Attribute();
        attr.id(id);
        attr.name(name);
        attr.order(order);
        attr.type(type);
        return attr;
    }
}
