package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 어플리케이션의 비즈니스 상태를 관리하는 모듈.
 * 모든 상태는 싱글톤으로 관리되어 여러 컴포넌트에서 공유된다.
 */
@Module
public interface StateModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static Observer<Render> renderObserver() { return behavior(null); }
    @Provides @Singleton static Observer<String> uriObserver() { return behavior(null); }
    
    // PageState, DocumentList, TypeProvider, SelectedRows 등 @Inject @Singleton 클래스들은 
    // Dagger가 자동으로 인스턴스화하여 관리하므로 별도 Provides 불필요.
}
