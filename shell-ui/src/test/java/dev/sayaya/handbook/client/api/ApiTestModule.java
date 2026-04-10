package dev.sayaya.handbook.client.api;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.api.MenuApi;
import dev.sayaya.handbook.client.interfaces.api.UserApi;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * UC-S8 테스트용 Dagger 모듈.
 * FetchApi를 FetchMock으로 제공하되 실제 UserApi/MenuApi 구현체를 사용한다.
 */
@Module
public class ApiTestModule {
    @Provides @Singleton static FetchApi provideFetchApi() {
        return new FetchMock();
    }
    @Provides @Singleton static BehaviorSubject<Progress> provideProgress() {
        return behavior(Progress.hide());
    }
    @Provides @Singleton static Observable<Progress> provideProgressObservable(BehaviorSubject<Progress> s) {
        return s.asObservable();
    }
    @Provides @Singleton static Observer<Progress> provideProgressObserver(BehaviorSubject<Progress> s) {
        return s;
    }
    @Provides @Singleton static BehaviorSubject<String> provideUri() {
        return behavior(null);
    }
    @Provides @Singleton static Observable<String> provideUriObservable(BehaviorSubject<String> uri) {
        return uri.asObservable();
    }
    @Provides @Singleton static ViewportObserver provideViewport() {
        return new ViewportObserver();
    }
    @Provides @Singleton static LanguageDetector provideLanguageDetector() {
        return () -> "en";
    }
    @Provides @Singleton static LanguagePackRepository provideLanguagePackRepository() {
        return lang -> behavior(Labels.empty());
    }
    @Provides @Singleton static MenuRepository menuRepo(MenuApi impl) {
        return impl;
    }
    @Provides @Singleton static UserRepository userRepo(UserApi impl) {
        return impl;
    }
}
