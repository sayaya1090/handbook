package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 워크스페이스 통계 VO. 서버 응답의 JSON을 그대로 매핑한다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class WorkspaceStats {
    public int typeCount;
    public int documentCount;
    public int userCount;
}
