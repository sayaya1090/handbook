package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 타입의 개별 속성을 표현하는 값 객체(GWT JsInterop native).
 *
 * <p><b>책임:</b> 백엔드 Attribute 엔티티의 GWT 호환 표현. 속성명, 순서, 데이터 타입,
 * nullable 여부, 상속 여부 등을 보유하며, with* 메서드로 불변 갱신을 지원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AttributeTypeValue} — 속성의 데이터 타입 정보</li>
 * </ul></p>
 * <p><b>주의:</b> @JsType(isNative=true) 클래스이므로 copy() 내부 구현으로 불변성을 보장한다.
 * inherited 필드는 부모 타입에서 상속된 속성인지를 나타낸다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class AttributeValue {
    public String name;
    public int order;
    public String description;
    public AttributeTypeValue type;
    public boolean nullable;
    public boolean inherited;

    @JsOverlay
    public static AttributeValue of(String name, int order, AttributeTypeValue type) {
        AttributeValue a = new AttributeValue();
        a.name = name;
        a.order = order;
        a.type = type;
        a.nullable = false;
        a.inherited = false;
        return a;
    }

    @JsOverlay
    public final AttributeValue withName(String name) {
        AttributeValue a = copy();
        a.name = name;
        return a;
    }

    @JsOverlay
    public final AttributeValue withType(AttributeTypeValue type) {
        AttributeValue a = copy();
        a.type = type;
        return a;
    }

    @JsOverlay
    public final AttributeValue withNullable(boolean nullable) {
        AttributeValue a = copy();
        a.nullable = nullable;
        return a;
    }

    @JsOverlay
    public final AttributeValue withDescription(String description) {
        AttributeValue a = copy();
        a.description = description;
        return a;
    }

    @JsOverlay
    private AttributeValue copy() {
        AttributeValue a = new AttributeValue();
        a.name = this.name;
        a.order = this.order;
        a.description = this.description;
        a.type = this.type;
        a.nullable = this.nullable;
        a.inherited = this.inherited;
        return a;
    }
}
