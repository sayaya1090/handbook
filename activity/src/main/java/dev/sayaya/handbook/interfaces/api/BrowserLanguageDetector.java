package dev.sayaya.handbook.interfaces.api;

import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.UserPreferences;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 브라우저 환경에서 언어를 감지한다.
 * localStorage "handbook.lang" → "lang" → navigator.language → "en" 순으로 폴백한다.
 *
 * <p><b>책임:</b> UserPreferences에 저장된 언어를 최우선으로 확인하고,
 * 없으면 레거시 키(lang), navigator.language 순으로 폴백한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link UserPreferences} — handbook.lang 키 조회</li>
 *   <li>{@link DomGlobal} — navigator.language 조회</li>
 * </ul></p>
 */
@Singleton
public class BrowserLanguageDetector implements LanguageDetector {

    /** localStorage 접근용 JsInterop 바인딩. */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Storage")
    private static class NativeStorage {
        public native String getItem(String key);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Window")
    private static class WindowWithStorage {
        public NativeStorage localStorage;
    }

    @Inject BrowserLanguageDetector() {}

    @Override
    public String detect() {
        String pref = UserPreferences.getLanguage();
        if (pref != null && !pref.isEmpty()) return pref.split("-")[0];
        // 레거시 키 'lang' 폴백
        String stored = jsinterop.base.Js.<WindowWithStorage>cast(DomGlobal.window).localStorage.getItem("lang");
        if (stored != null && !stored.isEmpty()) return stored.split("-")[0];
        String nav = DomGlobal.navigator.language;
        if (nav != null && !nav.isEmpty()) return nav.split("-")[0];
        return "en";
    }
}
