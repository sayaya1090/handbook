package dev.sayaya.handbook.client.interfaces.api;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

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
}
