package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Type;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.Date;
import java.util.List;

import static elemental2.core.Global.JSON;
import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class TypeNative {
    @JsProperty public String id;
    @JsProperty public String version;
    @JsProperty(name = "effect_date_time") public Double effectDateTime;
    @JsProperty(name = "expire_date_time") public Double expireDateTime;
    @JsProperty public String description;
    @JsProperty public boolean primitive;
    @JsProperty public List<Attribute> attributes; // 배열
    @JsProperty public String parent;
    @JsOverlay @JsIgnore public Type toType() {
        return new Type(
            id,
            version,
            effectDateTime != null ? new Date(effectDateTime.longValue()) : null,
            expireDateTime != null ? new Date(expireDateTime.longValue()) : null,
            description,
            primitive,
            attributes != null ? attributes : List.of(),
            parent
        );
    }
    @JsOverlay @JsIgnore private static TypeNative from(Type type) {
        if (type == null) return null;
        var nativeType = new TypeNative();
        nativeType.id = type.id();
        nativeType.version = type.version();
        nativeType.effectDateTime = type.effectDateTime()!=null?Long.valueOf(type.effectDateTime().getTime()).doubleValue():null;
        nativeType.expireDateTime = type.expireDateTime()!=null?Long.valueOf(type.expireDateTime().getTime()).doubleValue():null;
        nativeType.description = type.description();
        nativeType.primitive = type.primitive();
        nativeType.attributes = type.attributes();
        nativeType.parent = type.parent();
        return nativeType;
    }
    @JsOverlay @JsIgnore public static String toJSON(Type type) {
        return JSON.stringify(from(type));
    }
}
