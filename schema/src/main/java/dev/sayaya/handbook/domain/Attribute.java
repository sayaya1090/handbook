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
    @JsonProperty("id") @JsProperty(name = "id") private String id;
    @JsonProperty("name") @JsProperty(name = "name") private String name;
    @JsonProperty("order") @JsProperty(name = "order") private int order;
    @JsonProperty("type") @JsProperty(name = "type") private AttributeType type;
    @JsonProperty("description") @JsProperty(name = "description") private String description;
    @JsonProperty("nullable") @JsProperty(name = "nullable") private boolean nullable;
    @JsonProperty("inherited") @JsProperty(name = "inherited") private boolean inherited;
    @JsonProperty("read_roles") @JsProperty(name = "read_roles") private String[] readRoles;
    @JsonProperty("write_roles") @JsProperty(name = "write_roles") private String[] writeRoles;

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

    @JsOverlay @JsIgnore
    public Attribute withName(String name) {
        Attribute attr = create(id(), name, order(), type());
        attr.description(description());
        attr.nullable(nullable());
        attr.inherited(inherited());
        attr.readRoles(readRoles());
        attr.writeRoles(writeRoles());
        return attr;
    }

    @JsOverlay @JsIgnore
    public Attribute withType(AttributeType type) {
        Attribute attr = create(id(), name(), order(), type);
        attr.description(description());
        attr.nullable(nullable());
        attr.inherited(inherited());
        attr.readRoles(readRoles());
        attr.writeRoles(writeRoles());
        return attr;
    }

    @JsOverlay @JsIgnore
    public Attribute withDescription(String description) {
        Attribute attr = create(id(), name(), order(), type());
        attr.description(description);
        attr.nullable(nullable());
        attr.inherited(inherited());
        attr.readRoles(readRoles());
        attr.writeRoles(writeRoles());
        return attr;
    }

    @JsOverlay @JsIgnore
    public Attribute cloneWithoutId() {
        Attribute attr = create(null, name(), order(), type());
        attr.description(description());
        attr.nullable(nullable());
        attr.inherited(inherited());
        attr.readRoles(readRoles());
        attr.writeRoles(writeRoles());
        return attr;
    }
}
