package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.DashboardRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

/**
 * 대시보드 API 계층의 Dagger 바인딩 모듈.
 */
@Module
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
    @Binds DashboardRepository bindDashboardRepository(DashboardApi impl);
}
