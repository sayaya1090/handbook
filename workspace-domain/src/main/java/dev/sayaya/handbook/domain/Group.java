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
public final class Group {
    private String id;
    private String workspace;
    private String name;
    private String description;

    @JsOverlay @JsIgnore
    public static GroupBuilder builder() {
        return new GroupBuilder();
    }

    public static class GroupBuilder {
        private String id;
        private String workspace;
        private String name;
        private String description;
        GroupBuilder() {}
        public GroupBuilder id(String id) {
            this.id = id;
            return this;
        }
        public GroupBuilder workspace(String workspace) {
            this.workspace = workspace;
            return this;
        }
        public GroupBuilder name(String name) {
            this.name = name;
            return this;
        }
        public GroupBuilder description(String description) {
            this.description = description;
            return this;
        }
        public Group build() {
            var instance = new Group();
            instance.id = id;
            instance.workspace = workspace;
            instance.name = name;
            instance.description = description;
            return instance;
        }
    }
}
