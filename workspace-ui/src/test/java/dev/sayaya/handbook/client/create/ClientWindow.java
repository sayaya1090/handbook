package dev.sayaya.handbook.client.create;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="window")
final class ClientWindow {
    public static BehaviorSubject<Progress> progress;   // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<Label> labels;
}
