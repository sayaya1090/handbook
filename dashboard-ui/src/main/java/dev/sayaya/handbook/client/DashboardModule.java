package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.rx.subject.AsyncSubject;

import javax.inject.Singleton;

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
