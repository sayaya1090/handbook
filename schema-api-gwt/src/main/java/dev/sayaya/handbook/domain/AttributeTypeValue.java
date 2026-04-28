package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * 속성의 데이터 타입을 표현하는 유니온 스타일 값 객체(GWT JsInterop native).
 *
 * <p><b>책임:</b> 백엔드 AttributeType sealed interface의 GWT 호환 표현.
 * 단일 native 클래스에 type 구분자(text/bool/number/date/enum/array/map/file/document)와
 * 타입별 검증 필드를 nullable로 포함하며, 정적 팩토리 메서드로 각 타입 인스턴스를 생성한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>자기참조 — array의 elementType, map의 keyType/valueType이 재귀적으로 AttributeTypeValue를 참조</li>
 * </ul></p>
 * <p><b>주의:</b> simplify()는 @JsOverlay 인스턴스 메서드에서 재귀 호출 시
 * GWT ReferenceError가 발생하므로 static 헬퍼(doSimplify)로 우회한다.
 * 별도로 {@link AttributeTypes#simplify(AttributeTypeValue)}도 같은 로직을 제공한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class AttributeTypeValue {
    /** 타입 구분자: "text", "bool", "number", "date", "enum", "array", "map", "file", "document" */
    public String type;

    // Text
    @JsProperty(name = "regex_patterns")
    public String[] regexPatterns;

    // Number
    public Double min;
    public Double max;

    // Date
    public Double after;
    public Double before;

    // Enum
    @JsProperty(name = "allowed_values")
    public String[] allowedValues;

    // Array
    @JsProperty(name = "element_type")
    public AttributeTypeValue elementType;

    // Map
    @JsProperty(name = "key_type")
    public AttributeTypeValue keyType;
    @JsProperty(name = "value_type")
    public AttributeTypeValue valueType;

    // File
    public String[] extensions;

    // Document
    @JsProperty(name = "referenced_type")
    public String referencedType;

    @JsOverlay
    public static AttributeTypeValue text() {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "text";
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue bool() {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "bool";
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue number(Double min, Double max) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "number";
        v.min = min;
        v.max = max;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue date(Double after, Double before) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "date";
        v.after = after;
        v.before = before;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue enumType(String[] allowedValues) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "enum";
        v.allowedValues = allowedValues;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue array(AttributeTypeValue elementType) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "array";
        v.elementType = elementType;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue map(AttributeTypeValue keyType, AttributeTypeValue valueType) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "map";
        v.keyType = keyType;
        v.valueType = valueType;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue file(String[] extensions) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "file";
        v.extensions = extensions;
        return v;
    }

    @JsOverlay
    public static AttributeTypeValue document(String referencedType) {
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = "document";
        v.referencedType = referencedType;
        return v;
    }

    @JsOverlay @JsIgnore
    public String simplify() {
        return doSimplify(this);
    }

    @JsOverlay
    private static String doSimplify(AttributeTypeValue atv) {
        if (atv == null || atv.type == null) return "unknown";
        if ("array".equals(atv.type)) return doSimplify(atv.elementType) + "[]";
        if ("map".equals(atv.type)) return "map<" + doSimplify(atv.keyType) + ", " + doSimplify(atv.valueType) + ">";
        if ("document".equals(atv.type)) return atv.referencedType != null ? atv.referencedType : "document";
        return atv.type;
    }
}
