package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

// 쉘과 모듈이 데이터를 교환하기 위한 공유객체
@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="window")
final class ClientWindow {
    public static BehaviorSubject<Progress> progress;   // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<String> uri;          // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<Render> renderer;     // 모듈에서 발행, 쉘에서 구독
    public static BehaviorSubject<Tool[]> tools;        // 쉘에서 발행, 모듈에서 구독
    public static BehaviorSubject<Label> labels;
    public static BehaviorSubject<Workspace> workspace;    // 쉘에서 발행, 쉘, 모듈에서 구독
}
