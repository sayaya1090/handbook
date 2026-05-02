package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { 
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
    @Binds TypeRepository typeRepository(TypeApi impl);
    @Binds LayoutRepository layoutRepository(LayoutApi impl);
}
