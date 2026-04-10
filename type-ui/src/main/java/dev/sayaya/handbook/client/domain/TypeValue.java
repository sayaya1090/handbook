package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * 타입 스키마를 표현하는 불변 스타일 값 객체(GWT JsInterop native).
 *
 * <p><b>책임:</b> 백엔드 Type 엔티티의 GWT 호환 표현. id, version, 유효기간, 속성 배열 등
 * 타입 메타데이터를 보유하며, with* 메서드로 새 인스턴스를 생성하는 copy-on-write 패턴을 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AttributeValue} — 속성 배열 보유</li>
 * </ul></p>
 * <p><b>주의:</b> 캔버스 위치(x/y/w/h)는 {@link dev.sayaya.handbook.client.usecase.PositionMap}에서,
 * 변경 상태는 {@link dev.sayaya.handbook.client.components.ChangeTracker}에서 별도 관리한다.
 * key()는 "id:version" 형식으로 타입을 유일하게 식별한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class TypeValue {
    public String id;
    public String version;
    @JsProperty(name = "effect_date_time")
    public double effectDateTime;
    @JsProperty(name = "expire_date_time")
    public double expireDateTime;
    public String description;
    public boolean primitive;
    public String parent;
    public AttributeValue[] attributes;
    public double rev;

    @JsOverlay
    public static TypeValue create(String id, String version, double effectDateTime, double expireDateTime) {
        TypeValue t = new TypeValue();
        t.id = id;
        t.version = version;
        t.effectDateTime = effectDateTime;
        t.expireDateTime = expireDateTime;
        t.primitive = false;
        t.attributes = new AttributeValue[0];
        t.rev = 0;
        return t;
    }

    @JsOverlay
    public final String key() {
        return id + ":" + version;
    }

    @JsOverlay
    public final TypeValue withAttributes(AttributeValue[] attributes) {
        TypeValue t = copy();
        t.attributes = attributes;
        return t;
    }

    @JsOverlay
    public final TypeValue withDescription(String description) {
        TypeValue t = copy();
        t.description = description;
        return t;
    }

    @JsOverlay
    public final TypeValue withParent(String parent) {
        TypeValue t = copy();
        t.parent = parent;
        return t;
    }

    @JsOverlay
    private TypeValue copy() {
        TypeValue t = new TypeValue();
        t.id = this.id;
        t.version = this.version;
        t.effectDateTime = this.effectDateTime;
        t.expireDateTime = this.expireDateTime;
        t.description = this.description;
        t.primitive = this.primitive;
        t.parent = this.parent;
        t.attributes = this.attributes;
        t.rev = this.rev;
        return t;
    }
}
