package dev.sayaya.handbook.domain;

/**
 * {@link AttributeTypeValue} 관련 유틸리티 클래스.
 *
 * <p><b>책임:</b> @JsOverlay 인스턴스 메서드에서 재귀 호출이 불가능한 GWT 제약을 우회하여,
 * AttributeTypeValue의 타입 문자열 단순화(simplify) 등 재귀 로직을 일반 static 메서드로 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AttributeTypeValue} — 변환 대상 native 타입</li>
 * </ul></p>
 * <p><b>주의:</b> @JsOverlay 인스턴스 메서드에서 재귀 호출 시 GWT ReferenceError가 발생하므로
 * 이 클래스의 static 메서드를 사용해야 한다.</p>
 */
public final class AttributeTypes {
    private AttributeTypes() {}

    public static String simplify(AttributeTypeValue type) {
        if (type == null || type.type == null) return "unknown";
        if ("array".equals(type.type)) return simplify(type.elementType) + "[]";
        if ("map".equals(type.type)) return "map<" + simplify(type.keyType) + ", " + simplify(type.valueType) + ">";
        if ("document".equals(type.type)) return type.referencedType != null ? type.referencedType : "document";
        return type.type;
    }
}
