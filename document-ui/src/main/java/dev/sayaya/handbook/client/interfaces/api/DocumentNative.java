package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class DocumentNative {
    @JsOverlay @JsIgnore
    public Document toDomain() {
        return Document.builder().build();
    }
}
