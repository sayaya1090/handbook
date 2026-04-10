package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.DocumentValue;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/** JS 객체와 DocumentValue 간 변환을 위한 JsInterop 네이티브 타입. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class DocumentNative {
    public String id;
    public String type;
    public String serial;
    public double effectDateTime;
    public double expireDateTime;
    public double createDateTime;
    public String creator;
    public JsPropertyMap<String> data;

    @JsOverlay
    public static DocumentValue toDocumentValue(DocumentNative src) {
        return Js.cast(src);
    }

    @JsOverlay
    public static DocumentNative fromDocumentValue(DocumentValue src) {
        return Js.cast(src);
    }
}
