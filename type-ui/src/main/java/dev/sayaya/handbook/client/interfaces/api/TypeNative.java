package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeValue;
import dev.sayaya.handbook.client.domain.TypeValue;
import elemental2.core.JsDate;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * JSON ↔ TypeValue 변환을 위한 JsInterop native 클래스.
 *
 * <p><b>책임:</b> 서버 JSON 응답을 {@link TypeValue} 도메인 객체로 변환(toDomain)하고,
 * 도메인 객체를 서버 전송용 JSON 구조로 역변환(fromDomain)한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeValue} — 도메인 변환 대상</li>
 *   <li>{@link AttributeNative} — 속성 배열의 JSON 변환 위임</li>
 *   <li>{@link elemental2.core.JsDate} — ISO 8601 날짜 문자열 ↔ epoch 밀리초 변환</li>
 * </ul></p>
 * <p><b>주의:</b> @JsType(isNative=true) 클래스이므로 생성자/필드만 사용 가능. 로직은 @JsOverlay static/final 메서드로 구현.</p>
 */
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
    public double rev;

    @JsOverlay
    public final TypeValue toDomain() {
        TypeValue t = TypeValue.create(id, version,
                new JsDate(effectDateTime).getTime(),
                new JsDate(expireDateTime).getTime());
        t.description = description;
        t.primitive = primitive;
        t.parent = parent;
        t.rev = rev;
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
        n.rev = type.rev;
        if (type.attributes != null) {
            n.attributes = new AttributeNative[type.attributes.length];
            for (int i = 0; i < type.attributes.length; i++) {
                n.attributes[i] = AttributeNative.fromDomain(type.attributes[i]);
            }
        }
        return n;
    }
}
