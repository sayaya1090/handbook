package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 타입별 문서 분포 값 객체.
 *
 * <p><b>책임:</b> 서버에서 수신한 타입별 문서 분포 JSON을 JsInterop으로 매핑한다.
 * 타입명과 해당 문서 수를 포함한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (네이티브 JsType, JSON 직접 매핑)</li></ul></p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DistributionData {
    /** 문서 타입명 */
    public String type;
    /** 해당 타입의 문서 수 */
    public int count;
}
