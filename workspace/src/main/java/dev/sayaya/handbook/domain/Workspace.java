package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Workspace {
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("name") @JsProperty private String name;
    @JsonProperty("description") @JsProperty private String description;

    @JsOverlay @JsIgnore
    public static Workspace create(String id, String name, String description) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID cannot be empty");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        Workspace ws = new Workspace();
        ws.id = id;
        ws.name = name;
        ws.description = description;
        return ws;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    @Getter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Accessors(fluent = true)
    @NoArgsConstructor
    public static final class WorkspaceSimple {
        @JsonProperty("id") @JsProperty private String id;
        @JsonProperty("name") @JsProperty private String name;

        @JsOverlay @JsIgnore
        public static WorkspaceSimple create(String id, String name) {
            if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID cannot be empty");
            if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
            WorkspaceSimple ws = new WorkspaceSimple();
            ws.id = id;
            ws.name = name;
            return ws;
        }
    }
}
