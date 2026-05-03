package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 에이전트 활동 이벤트 값 객체.
 *
 * <p><b>책임:</b> 서버에서 수신한 에이전트 활동 JSON을 JsInterop으로 매핑한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (네이티브 JsType, JSON 직접 매핑)</li></ul></p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AgentActivity {
    public double timestamp;
    public String intent;
    public int commandCount;
    public String status;
}
