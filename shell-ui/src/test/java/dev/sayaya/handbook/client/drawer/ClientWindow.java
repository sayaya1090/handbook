package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

// 쉘과 모듈이 데이터를 교환하기 위한 공유객체
@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="window")
final class ClientWindow {
    public static BehaviorSubject<Tool[]> tools;        // 쉘에서 발행, 모듈에서 구독
}
