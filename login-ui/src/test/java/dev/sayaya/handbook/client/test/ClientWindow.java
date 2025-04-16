package dev.sayaya.handbook.client.test;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="window")
final class ClientWindow {
    public static BehaviorSubject<Progress> progress;   // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<String> uri;          // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<Render> renderer;     // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<Tool[]> tools;        // 쉘에서 발행, 모듈에서 구독
    public static BehaviorSubject<Label> labels;
}
