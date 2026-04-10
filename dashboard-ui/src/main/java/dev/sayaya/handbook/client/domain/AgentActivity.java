package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 에이전트 활동 VO. 에이전트가 발행한 이벤트를 나타낸다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AgentActivity {
    public double timestamp;
    public String intent;
    public int commandCount;
    public String status;
}
