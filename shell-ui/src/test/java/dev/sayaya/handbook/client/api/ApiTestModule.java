package dev.sayaya.handbook.client.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.StateModule;
import dev.sayaya.handbook.client.interfaces.api.MenuApi;
import dev.sayaya.handbook.client.interfaces.api.UserApi;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Singleton;

/**
 * 테스트 전용 독립적인 Dagger 모듈.
 * 불필요한 UI 엘리먼트 의존성을 배제하기 위해 ShellModule 대신 StateModule만 포함한다.
 */
@Module(includes = StateModule.class)
public interface ApiTestModule {
    @Provides @Singleton static FetchApi provideFetchApi() { return new FetchMock(); }
    
    // 리포지토리 모킹
    @Binds UserRepository userRepo(UserApi impl);
    @Binds MenuRepository menuRepo(MenuApi impl);
    @Binds WorkspaceRepository workspaceRepo(dev.sayaya.handbook.client.interfaces.api.WorkspaceApi impl);

    @Provides @Singleton static ViewportObserver provideViewport() { return new ViewportObserver(); }
    @Provides @Singleton static LanguageDetector provideLanguageDetector() { return () -> "en"; }
    @Provides @Singleton static LanguagePackRepository provideLanguagePackRepository() { return lang -> dev.sayaya.rx.subject.BehaviorSubject.behavior(dev.sayaya.handbook.domain.Labels.empty()); }
    
    @Provides @Singleton static ScriptInjector provideScriptInjector() { return src -> {}; }
    @Provides @Singleton static SessionEnvironment provideSessionEnvironment() {
        return new SessionEnvironment() {
            @Override public String getCookies() { return ""; }
            @Override public Double getJwtClaimAsDouble(String token, String claim) { return null; }
            @Override public void redirect(String path) {}
            @Override public void clearInterval(double handle) {}
        };
    }
}


