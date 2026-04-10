package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 품질 이슈 VO. 정합성 검증 결과를 나타낸다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class QualityIssue {
    public String type;
    public String serial;
    public String field;
    public String severity;
    public String message;
}
