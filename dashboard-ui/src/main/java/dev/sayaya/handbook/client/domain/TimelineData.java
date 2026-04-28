package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 타임라인 통계의 단일 시간 구간 값 객체.
 *
 * <p><b>책임:</b> 서버에서 수신한 타임라인 통계 JSON을 JsInterop으로 매핑한다.
 * 날짜별 문서 수, 검증 실패 수, 에이전트 명령 수를 포함한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (네이티브 JsType, JSON 직접 매핑)</li></ul></p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TimelineData {
    /** 구간의 시작 날짜 (ISO-8601 문자열) */
    public String date;
    /** 해당 구간의 문서 수 */
    public int documentCount;
    /** 해당 구간의 검증 실패 수 */
    public int validationFailures;
    /** 해당 구간의 에이전트 명령 수 */
    public int agentCommands;
}
