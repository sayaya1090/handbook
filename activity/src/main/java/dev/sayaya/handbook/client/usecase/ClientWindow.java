package dev.sayaya.handbook.client.usecase;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

// 쉘과 모듈이 데이터를 교환하기 위한 공유객체
@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="window")
public final class ClientWindow {
    public static ProgressSubject progress;     // 모듈에서 발행, 쉘에서 구독
    public static UriSubject uri;               // (쉘 or 모듈)에서 발행, 모듈에서 구독
    public static RendererSubject renderer;     // 모듈에서 발행, 쉘에서 구독
    public static ToolSubject tools;    // 쉘에서 발행, 모듈에서 구독
}
