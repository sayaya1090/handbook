package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.UserPreferences;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;

/**
 * 대시보드 모듈의 Dagger 공통 바인딩 모듈.
 *
 * <p><b>책임:</b> LanguageDetector와 LanguagePackRepository를 제공하여 대시보드 UI의 다국어 처리를 지원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LanguageDetector} — localStorage/navigator에서 언어 코드 감지</li>
 *   <li>{@link LanguagePackRepository} — FetchApi로 언어팩 JSON 로드</li>
 * </ul></p>
 * <p><b>주의:</b> detectLanguage()는 Elemental2를 통해 브라우저 환경에서 동작한다.</p>
 */
@Module
public class DashboardModule {
    @Provides @Singleton static LanguageDetector languageDetector() {
        return DashboardModule::detectLanguage;
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

    /**
     * 브라우저 환경에서 사용자 언어를 감지한다.
     * UserPreferences(handbook.lang) -> navigator.language -> "en" 순으로 폴백한다.
     */
    private static String detectLanguage() {
        String stored = UserPreferences.getLanguage();
        if (stored != null && !stored.isEmpty()) return stored.split("-")[0];
        String nav = DomGlobal.navigator.language;
        if (nav != null && !nav.isEmpty()) return nav.split("-")[0];
        return "en";
    }
}
