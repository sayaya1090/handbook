package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 속성 정보. 타입의 속성을 Handsontable 컬럼으로 변환할 때 사용한다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AttributeInfo {
    public String name;
    public String type;
    public boolean nullable;
    public String description;
}
