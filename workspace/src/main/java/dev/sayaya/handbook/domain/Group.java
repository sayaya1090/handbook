package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Group {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("workspace") @JsProperty public String workspace;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("description") @JsProperty public String description;

    @JsOverlay @JsIgnore
    public static Group create(String id, String workspace, String name, String description) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID cannot be empty");
        if (workspace == null || workspace.trim().isEmpty()) throw new IllegalArgumentException("Workspace ID cannot be empty");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        Group group = new Group();
        group.id = id;
        group.workspace = workspace;
        group.name = name;
        group.description = description;
        return group;
    }
}
