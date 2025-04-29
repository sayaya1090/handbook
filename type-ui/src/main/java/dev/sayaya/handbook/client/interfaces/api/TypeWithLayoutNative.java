package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Box;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.List;

import static elemental2.core.Global.JSON;
import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class TypeWithLayoutNative {
    @JsProperty public TypeNative type;
    @JsProperty public double x;
    @JsProperty public double y;
    @JsProperty public double width;
    @JsProperty public double height;
    @JsOverlay @JsIgnore private static TypeWithLayoutNative from(Box box) {
        if (box == null) return null;
        var nativeType = new TypeWithLayoutNative();

        nativeType.type = TypeNative.from(box.type());
        nativeType.x = box.x();
        nativeType.y = box.y();
        nativeType.width = box.width();
        nativeType.height = box.height();
        return nativeType;
    }
    @JsOverlay @JsIgnore public static String toJSON(Box box) {
        return JSON.stringify(from(box));
    }
    @JsOverlay @JsIgnore public static String toJSON(List<Box> boxes) {
        var array = boxes.stream().map(TypeWithLayoutNative::from).toArray(size->new TypeWithLayoutNative[size]);
        return JSON.stringify(array);
    }
}
