package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

/** GWT용 문서 VO. 서버 응답의 JSON을 그대로 매핑한다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DocumentValue {
    public String id;
    public String type;
    public String serial;
    public double effectDateTime;
    public double expireDateTime;
    public double createDateTime;
    public String creator;
    public JsPropertyMap<String> data;
    public double rev;
}
