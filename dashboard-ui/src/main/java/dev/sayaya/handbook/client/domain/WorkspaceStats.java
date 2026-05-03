package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 워크스페이스 통계 값 객체.
 *
 * <p><b>책임:</b> 서버에서 수신한 워크스페이스 통계(타입 수, 문서 수, 사용자 수) JSON을 JsInterop으로 매핑한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (네이티브 JsType, JSON 직접 매핑)</li></ul></p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class WorkspaceStats {
    public int typeCount;
    public int documentCount;
    public int userCount;
}
