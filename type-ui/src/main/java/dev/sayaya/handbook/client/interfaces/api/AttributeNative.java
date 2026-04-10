package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.AttributeValue;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * JSON ↔ AttributeValue 변환용 네이티브 JsInterop 객체.
 *
 * <p><b>책임:</b> 서버 JSON의 snake_case 필드명과 도메인 객체 간 매핑을 처리한다.
 * readRoles/writeRoles 필드를 통해 필드 레벨 권한 정보를 전달한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AttributeValue} — 변환 대상 도메인 값 객체</li>
 *   <li>{@link AttributeTypeValue} — 속성 데이터 타입 정보</li>
 * </ul></p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AttributeNative {
    public String name;
    public int order;
    public String description;
    @JsProperty(name = "attribute_type") public AttributeTypeValue type;
    public boolean nullable;
    public boolean inherited;
    /** 이 속성을 읽을 수 있는 역할 목록 */
    @JsProperty(name = "read_roles") public String[] readRoles;
    /** 이 속성을 쓸 수 있는 역할 목록 */
    @JsProperty(name = "write_roles") public String[] writeRoles;

    @JsOverlay
    public final AttributeValue toDomain() {
        AttributeValue a = AttributeValue.of(name, order, type);
        a.description = description;
        a.nullable = nullable;
        a.inherited = inherited;
        a.readRoles = readRoles != null ? readRoles : new String[0];
        a.writeRoles = writeRoles != null ? writeRoles : new String[0];
        return a;
    }

    @JsOverlay
    public static AttributeNative fromDomain(AttributeValue attr) {
        AttributeNative n = new AttributeNative();
        n.name = attr.name;
        n.order = attr.order;
        n.description = attr.description;
        n.type = attr.type;
        n.nullable = attr.nullable;
        n.inherited = attr.inherited;
        n.readRoles = attr.readRoles != null ? attr.readRoles : new String[0];
        n.writeRoles = attr.writeRoles != null ? attr.writeRoles : new String[0];
        return n;
    }
}
