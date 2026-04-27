package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Singleton;

@Module
public interface WorkspaceApiModule {
    @Binds WorkspaceApi bindWorkspaceApi(WorkspaceApiImpl impl);

    @Provides
    @Singleton
    static FetchApi provideFetchApi() {
        return new FetchApi() {
            @Override
            public Promise<Response> request(String url, RequestInit param) {
                return DomGlobal.fetch(url, param);
            }
        };
    }

    @Binds
    @Singleton
    LanguageDetector bindLanguageDetector(dev.sayaya.handbook.client.interfaces.api.BrowserLanguageDetector impl);

    @Binds
    @Singleton
    LanguagePackRepository bindLanguagePackRepository(dev.sayaya.handbook.client.interfaces.api.FetchLanguagePackRepository impl);
}
