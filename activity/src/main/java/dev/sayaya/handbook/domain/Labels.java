package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * 다국어 번역 레이블.
 * JSON 언어팩에서 로드된 key-value 맵을 JsInterop으로 래핑한다.
 *
 * <p>사용 예:
 * <pre>
 * labels.get("documents")        → "마스터 데이터" (ko) / "Master Data" (en)
 * labels.get("unknown_key")      → "unknown_key" (키 자체 반환)
 * labels.getOrDefault("k", "?")  → "?" (키 없으면 기본값)
 * </pre>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Labels {

    /** 키에 해당하는 번역 문자열을 반환한다. 키가 없으면 키 자체를 반환한다. */
    @JsOverlay @JsIgnore
    public String get(String key) {
        if (key == null) return null;
        @SuppressWarnings("unchecked")
        JsPropertyMap<String> map = (JsPropertyMap<String>)(JsPropertyMap<?>) Js.asPropertyMap(this);
        return map.has(key) ? map.get(key) : key;
    }

    /** 키에 해당하는 번역 문자열을 반환한다. 키가 없으면 defaultValue를 반환한다. */
    @JsOverlay @JsIgnore
    public String getOrDefault(String key, String defaultValue) {
        if (key == null) return defaultValue;
        @SuppressWarnings("unchecked")
        JsPropertyMap<String> map = (JsPropertyMap<String>)(JsPropertyMap<?>) Js.asPropertyMap(this);
        return map.has(key) ? map.get(key) : defaultValue;
    }

    /** 빈 Labels 인스턴스를 생성한다. */
    @JsOverlay @JsIgnore
    public static Labels empty() {
        return new Labels();
    }
}
