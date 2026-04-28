package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Group {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("workspace") @JsProperty public String workspace;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("description") @JsProperty public String description;

    @JsOverlay @JsIgnore
    public static Group create(String id, String workspace, String name, String description) {
        Group group = new Group();
        group.id = id;
        group.workspace = workspace;
        group.name = name;
        group.description = description;
        return group;
    }
}
