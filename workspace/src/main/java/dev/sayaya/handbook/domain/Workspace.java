package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Workspace {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("description") @JsProperty public String description;

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
    public static final class WorkspaceSimple {
        @JsonProperty("id") @JsProperty public String id;
        @JsonProperty("name") @JsProperty public String name;

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
