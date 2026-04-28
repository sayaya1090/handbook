package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Workspace {
    private String id;
    private String name;

    @JsOverlay @JsIgnore
    public static WorkspaceBuilder builder() {
        return new WorkspaceBuilder();
    }

    public static class WorkspaceBuilder {
        private String id;
        private String name;
        WorkspaceBuilder() {}
        public WorkspaceBuilder id(String id) {
            this.id = id;
            return this;
        }
        public WorkspaceBuilder name(String name) {
            this.name = name;
            return this;
        }
        public Workspace build() {
            var instance = new Workspace();
            instance.id = id;
            instance.name = name;
            return instance;
        }
        public String toString() {
            return "Workspace.WorkspaceBuilder(id=" + this.id + ", name=" + this.name + ")";
        }
    }
}
