package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.onboarding.UiModule;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.*;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;

import javax.inject.Singleton;

@Module(includes = { ApiModule.class, StateModule.class, UiModule.class })
public class OnboardingModule {
    @Provides @Singleton static LanguageDetector languageDetector() {
        return OnboardingModule::detectLanguage;
    }
    
    @Provides @Singleton static LanguagePackRepository languagePackRepository(dev.sayaya.handbook.usecase.FetchApi fetchApi) {
        return lang -> {
            Promise<Labels> promise = fetchApi.request("js/language." + lang + ".json")
                    .then(r -> r.ok ? r.json() : Promise.reject("HTTP " + r.status))
                    .then(obj -> Promise.resolve((Labels) obj))
                    .catch_(err -> Promise.resolve(Labels.empty()));
            return AsyncSubject.await(promise);
        };
    }
    
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return AgentMutation.receiver();
    }

    private static String detectLanguage() {
        String stored = UserPreferences.getLanguage();
        if (stored != null && !stored.isEmpty()) return stored.split("-")[0];
        String nav = DomGlobal.navigator.language;
        if (nav != null && !nav.isEmpty()) return nav.split("-")[0];
        return "en";
    }
}
