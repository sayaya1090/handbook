package dev.sayaya.handbook.client.domain;

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
    static Workspace of(String id, String name) {
        Workspace workspace = new Workspace();
        workspace.id = id;
        workspace.name = name;
        return workspace;
    }
    @JsOverlay @JsIgnore
    public static WorkspaceBuilder builder() { return new WorkspaceBuilder(); }
}
