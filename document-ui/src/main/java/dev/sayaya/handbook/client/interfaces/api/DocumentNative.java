package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.DocumentValue;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * JS 객체와 DocumentValue 간 변환을 위한 JsInterop 네이티브 타입.
 *
 * <p><b>책임:</b> 서버 API로부터 수신된 JSON 객체를 Java 측 {@link dev.sayaya.handbook.client.domain.DocumentValue}로
 * 캐스팅하거나, 반대로 DocumentValue를 네이티브 JS 객체로 변환한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.handbook.client.domain.DocumentValue} — 변환 대상 도메인 객체</li>
 *   <li>{@link jsinterop.base.Js} — 타입 간 캐스팅 유틸리티</li>
 * </ul></p>
 *
 * <p><b>주의:</b> native JS Object로 매핑되므로 Js.cast()를 통한 타입 변환만 가능하다.
 * DocumentValue와 동일한 필드 구조를 가져야 캐스팅이 올바르게 동작한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class DocumentNative {
    public String id;
    public String type;
    public String serial;
    public double effectDateTime;
    public double expireDateTime;
    public double createDateTime;
    public String creator;
    public JsPropertyMap<String> data;
    public String status;
    public double rev;

    @JsOverlay
    public static DocumentValue toDocumentValue(DocumentNative src) {
        return Js.cast(src);
    }

    @JsOverlay
    public static DocumentNative fromDocumentValue(DocumentValue src) {
        return Js.cast(src);
    }
}
