package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 사용자 설정을 localStorage에서 읽고 쓰는 유틸리티.
 *
 * <p><b>책임:</b> {@code handbook.lang}과 {@code handbook.theme} 키를 통해
 * 사용자의 언어 및 테마 설정을 관리한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>없음 (브라우저 localStorage만 사용)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> JsInterop을 통해 localStorage에 접근한다.
 * 서버 사이드에서는 사용할 수 없다.</p>
 */
public class UserPreferences {

    private static final String KEY_LANG = "handbook.lang";
    private static final String KEY_THEME = "handbook.theme";

    /** 브라우저 Storage API를 JsInterop으로 매핑한다. */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Storage")
    private static class NativeStorage {
        public native String getItem(String key);
        public native void setItem(String key, String value);
    }

    /** window.localStorage를 JsInterop으로 접근하기 위한 Window 확장 타입. */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Window")
    private static class WindowWithStorage {
        public NativeStorage localStorage;
    }

    private UserPreferences() {}

    /** localStorage 인스턴스를 반환한다. */
    private static NativeStorage localStorage() {
        return jsinterop.base.Js.<WindowWithStorage>cast(DomGlobal.window).localStorage;
    }

    /** 저장된 언어 코드를 반환한다. 설정이 없으면 null을 반환한다. */
    public static String getLanguage() {
        return localStorage().getItem(KEY_LANG);
    }

    /** 언어 코드를 저장한다. */
    public static void setLanguage(String lang) {
        localStorage().setItem(KEY_LANG, lang);
    }

    /** 저장된 테마를 반환한다 ("light" 또는 "dark"). 설정이 없으면 null을 반환한다. */
    public static String getTheme() {
        return localStorage().getItem(KEY_THEME);
    }

    /** 테마를 저장한다. */
    public static void setTheme(String theme) {
        localStorage().setItem(KEY_THEME, theme);
    }
}
