package dev.sayaya.handbook.client.interfaces.api;

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
    @JsProperty public AttributeNative[] attributes; // 배열
    @JsProperty public String parent;
    @JsProperty public double x;
    @JsProperty public double y;
    @JsProperty public double width;
    @JsProperty public double height;
    @JsProperty public boolean delete;
    @JsOverlay @JsIgnore public Type toDomain() {
        return Type.builder().id(id).version(version)
                .effectDateTime(effectDateTime != null ? new Date(effectDateTime.longValue()) : null)
                .expireDateTime(expireDateTime != null ? new Date(expireDateTime.longValue()) : null)
                .description(description).primitive(primitive)
                .attributes(List.of())
                .parent(parent)
                .x((int)x).y((int)y).width((int)width).height((int)height)
                .build();
    }
    @JsOverlay @JsIgnore public static TypeNative from(Type type, boolean delete) {
        if (type == null) return null;
        var nativeType = new TypeNative();
        nativeType.id = type.id();
        nativeType.version = type.version();
        nativeType.effectDateTime = type.effectDateTime()!=null?Long.valueOf(type.effectDateTime().getTime()).doubleValue():null;
        nativeType.expireDateTime = type.expireDateTime()!=null?Long.valueOf(type.expireDateTime().getTime()).doubleValue():null;
        nativeType.description = type.description();
        nativeType.primitive = type.primitive();
        nativeType.attributes = new AttributeNative[0];
        nativeType.parent = type.parent();
        nativeType.x = type.x();
        nativeType.y = type.y();
        nativeType.width = type.width();
        nativeType.height = type.height();
        nativeType.delete = delete;
        return nativeType;
    }
}
