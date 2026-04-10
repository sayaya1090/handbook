package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeValue;
import dev.sayaya.handbook.client.domain.TypeValue;
import elemental2.core.JsDate;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** JSON ↔ TypeValue 변환을 위한 JsInterop native 클래스. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TypeNative {
    public String id;
    public String version;
    @JsProperty(name = "effect_date_time") public String effectDateTime;
    @JsProperty(name = "expire_date_time") public String expireDateTime;
    public String description;
    public boolean primitive;
    public String parent;
    public AttributeNative[] attributes;

    @JsOverlay
    public final TypeValue toDomain() {
        TypeValue t = TypeValue.create(id, version,
                new JsDate(effectDateTime).getTime(),
                new JsDate(expireDateTime).getTime());
        t.description = description;
        t.primitive = primitive;
        t.parent = parent;
        if (attributes != null) {
            AttributeValue[] attrs = new AttributeValue[attributes.length];
            for (int i = 0; i < attributes.length; i++) {
                attrs[i] = attributes[i].toDomain();
            }
            t.attributes = attrs;
        }
        return t;
    }

    @JsOverlay
    public static TypeNative fromDomain(TypeValue type) {
        TypeNative n = new TypeNative();
        n.id = type.id;
        n.version = type.version;
        n.effectDateTime = new JsDate(type.effectDateTime).toISOString();
        n.expireDateTime = new JsDate(type.expireDateTime).toISOString();
        n.description = type.description;
        n.primitive = type.primitive;
        n.parent = type.parent;
        if (type.attributes != null) {
            n.attributes = new AttributeNative[type.attributes.length];
            for (int i = 0; i < type.attributes.length; i++) {
                n.attributes[i] = AttributeNative.fromDomain(type.attributes[i]);
            }
        }
        return n;
    }
}
