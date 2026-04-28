package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 타입 정보. 컬럼 정의를 위해 속성 목록을 포함한다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TypeInfo {
    public String id;
    public String version;
    public AttributeInfo[] attributes;
}
