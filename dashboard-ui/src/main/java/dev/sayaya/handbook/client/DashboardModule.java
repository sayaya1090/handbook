package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.rx.subject.AsyncSubject;

import javax.inject.Singleton;

/**
 * 대시보드 모듈의 Dagger 공통 바인딩 모듈.
 *
 * <p><b>책임:</b> LanguageDetector와 LanguagePackRepository를 제공하여 대시보드 UI의 다국어 처리를 지원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LanguageDetector} — localStorage/navigator에서 언어 코드 감지</li>
 *   <li>{@link LanguagePackRepository} — FetchApi로 언어팩 JSON 로드</li>
 * </ul></p>
 * <p><b>주의:</b> detectLanguage()는 JSNI로 구현되어 브라우저 환경에서만 동작한다.</p>
 */
@Module
public class DashboardModule {
    @Provides @Singleton static LanguageDetector languageDetector() {
        return () -> detectLanguage();
    }
    @Provides @Singleton static LanguagePackRepository languagePackRepository(dev.sayaya.handbook.usecase.FetchApi fetchApi) {
        return lang -> {
            elemental2.promise.Promise<Labels> promise = fetchApi.request("js/language." + lang + ".json")
                    .then(r -> r.ok ? r.json() : elemental2.promise.Promise.reject("HTTP " + r.status))
                    .then(obj -> elemental2.promise.Promise.resolve((Labels) obj))
                    .catch_(err -> elemental2.promise.Promise.resolve(Labels.empty()));
            return AsyncSubject.await(promise);
        };
    }

    private static native String detectLanguage() /*-{
        var stored = $wnd.localStorage.getItem('lang');
        if (stored) return stored.split('-')[0];
        var nav = $wnd.navigator.language;
        if (nav) return nav.split('-')[0];
        return 'en';
    }-*/;
}
