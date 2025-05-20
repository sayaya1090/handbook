package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Type;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class TypeNative {
    @JsProperty public String id;
    @JsProperty public String version;
    @JsProperty public String prev;
    @JsProperty public String next;
    @JsProperty(name = "effect_date_time") public Double effectDateTime;
    @JsProperty(name = "expire_date_time") public Double expireDateTime;
    @JsProperty public String description;
    @JsProperty public boolean primitive;
    @JsProperty public AttributeNative[] attributes; // 배열
    @JsProperty public String parent;
    @JsOverlay @JsIgnore public Type toDomain() {
        return Type.builder().id(id).version(version).prev(prev).next(next)
                .effectDateTime(effectDateTime != null ? new Date(effectDateTime.longValue()) : null)
                .expireDateTime(expireDateTime != null ? new Date(expireDateTime.longValue()) : null)
                .description(description).primitive(primitive)
                .attributes(attributes != null ? IntStream.range(0, attributes.length)
                        .mapToObj(i -> attributes[i].toDomain(id, version, i))
                        .collect(Collectors.toList()) : List.of()
                ).parent(parent)
                .build();
    }
}
