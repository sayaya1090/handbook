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
public final class Workspace implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
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
    public static final class WorkspaceSimple implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
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
