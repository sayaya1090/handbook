package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.client.usecase.AuthRepository;
import dev.sayaya.handbook.client.usecase.ScriptInjector;
import dev.sayaya.handbook.client.usecase.SessionEnvironment;
import dev.sayaya.handbook.interfaces.api.BrowserLanguageDetector;
import dev.sayaya.handbook.interfaces.api.FetchLanguagePackRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Singleton;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return (url, param) -> elemental2.dom.DomGlobal.fetch(url, param); }
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

