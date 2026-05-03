package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.interfaces.api.BrowserLanguageDetector;
import dev.sayaya.handbook.interfaces.api.FetchLanguagePackRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Singleton;

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
    @Binds MenuRepository menuRepositoryProvider(MenuApi impl);
    @Binds UserRepository userRepositoryProvider(UserApi impl);
    @Binds WorkspaceRepository workspaceRepositoryProvider(WorkspaceApi impl);
    @Binds AuthRepository authRepositoryProvider(AuthApi impl);

    @Binds @Singleton LanguageDetector bindLanguageDetector(BrowserLanguageDetector impl);

    @Binds @Singleton LanguagePackRepository bindLanguagePackRepository(FetchLanguagePackRepository impl);
    @Binds @Singleton ScriptInjector bindScriptInjector(NativeScriptInjector impl);
    @Binds @Singleton SessionEnvironment bindSessionEnvironment(NativeSessionEnvironment impl);
    
    @Provides @Singleton static ViewportObserver provideViewportObserver() { return new ViewportObserver(); }
    @Provides @Singleton static ToastContainer provideToastContainer() { return new ToastContainer(); }
}

