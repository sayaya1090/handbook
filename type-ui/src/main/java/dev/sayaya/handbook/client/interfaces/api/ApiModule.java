package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public interface ApiModule {
    @Provides @Singleton static FetchApi fetch() { 
        return new FetchApi() {
            @Override
            public elemental2.promise.Promise<elemental2.dom.Response> request(String url) {
                return elemental2.dom.DomGlobal.window.fetch(url);
            }
            @Override
            public elemental2.promise.Promise<elemental2.dom.Response> request(String url, elemental2.dom.RequestInit init) {
                return elemental2.dom.DomGlobal.window.fetch(url, init);
            }
        };
    }

    @Provides @Singleton static dev.sayaya.handbook.usecase.LanguagePackRepository languagePackRepository(FetchApi fetchApi) {
        return lang -> {
            elemental2.promise.Promise<dev.sayaya.handbook.domain.Labels> promise = fetchApi.request("js/language." + lang + ".json")
                    .then(r -> r.ok ? r.json() : elemental2.promise.Promise.reject("HTTP " + r.status))
                    .then(obj -> elemental2.promise.Promise.resolve((dev.sayaya.handbook.domain.Labels) obj))
                    .catch_(err -> elemental2.promise.Promise.resolve(dev.sayaya.handbook.domain.Labels.empty()));
            return dev.sayaya.rx.subject.AsyncSubject.await(promise);
        };
    }

    @Binds TypeRepository typeRepository(TypeApi impl);
    @Binds LayoutRepository layoutRepository(LayoutApi impl);
}
