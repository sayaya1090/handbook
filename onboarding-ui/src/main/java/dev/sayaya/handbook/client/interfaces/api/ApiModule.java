package dev.sayaya.handbook.client.interfaces.api;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Singleton;

/**
 * 온보딩 모듈의 핵심 API 연동을 담당하는 Dagger 모듈.
 */
@Module(includes = { WorkspaceModule.class })
public interface ApiModule {
    @Provides static FetchApi fetch() { 
        return new FetchApi() {
            @Override
            public Promise<Response> request(String url) {
                return DomGlobal.window.fetch(url);
            }

            @Override
            public Promise<Response> request(String url, RequestInit init) {
                return DomGlobal.window.fetch(url, init);
            }
        };
    }
    @Provides @Singleton static ViewportObserver provideViewportObserver() { return new ViewportObserver(); }
}
