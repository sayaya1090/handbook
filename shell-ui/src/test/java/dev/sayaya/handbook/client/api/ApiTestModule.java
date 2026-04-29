package dev.sayaya.handbook.client.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.ShellModule;
import dev.sayaya.handbook.client.interfaces.api.MenuApi;
import dev.sayaya.handbook.client.interfaces.api.UserApi;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

/**
 * 테스트 전용 독립적인 Dagger 모듈.
 */
@Module(includes = Module.class)
public interface ApiTestModule {
    @Provides @Singleton static FetchApi provideFetchApi() { return new FetchMock(); }
    
    // 리포지토리 모킹
    @Binds UserRepository userRepo(UserApi impl);
    @Binds MenuRepository menuRepo(MenuApi impl);
    @Binds WorkspaceRepository workspaceRepo(dev.sayaya.handbook.client.interfaces.api.WorkspaceApi impl);

    @Provides @Singleton static ViewportObserver provideViewport() { return new ViewportObserver(); }
    @Provides @Singleton static LanguageDetector provideLanguageDetector() { return () -> "en"; }
    @Provides @Singleton static LanguagePackRepository provideLanguagePackRepository() { return lang -> dev.sayaya.rx.subject.BehaviorSubject.behavior(dev.sayaya.handbook.domain.Labels.empty()); }
}
