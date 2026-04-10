package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.usecase.LanguageDetector;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 브라우저 환경에서 언어를 감지한다.
 * localStorage "lang" → navigator.language → "en" 순으로 폴백한다.
 */
@Singleton
public class BrowserLanguageDetector implements LanguageDetector {
    @Inject BrowserLanguageDetector() {}

    @Override
    public native String detect() /*-{
        var stored = $wnd.localStorage.getItem('lang');
        if (stored) return stored.split('-')[0];
        var nav = $wnd.navigator.language;
        if (nav) return nav.split('-')[0];
        return 'en';
    }-*/;
}
