package dev.sayaya.handbook.client.domain;

/** AttributeTypeValue 관련 유틸. native 타입의 @JsOverlay 한계를 우회한다. */
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
