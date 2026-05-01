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
public final class Attribute implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("order") @JsProperty public int order;
    @JsonProperty("type") @JsProperty public AttributeType type;
    @JsonProperty("description") @JsProperty public String description;
    @JsonProperty("nullable") @JsProperty public boolean nullable;
    @JsonProperty("inherited") @JsProperty public boolean inherited;
    @JsonProperty("readRoles") @JsProperty public String[] readRoles;
    @JsonProperty("writeRoles") @JsProperty public String[] writeRoles;

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
