package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static elemental2.dom.DomGlobal.window;

@Module
public abstract class HostSharedModule {
    @Provides @Singleton static ViewportObserver viewport() {
        return new ViewportObserver();
    }
    @Provides @Singleton static BehaviorSubject<String> uri() {
        return behavior(window.location.href);
    }
    @Provides @Singleton static BehaviorSubject<Render> render() {
        return behavior(null);
    }
    @Provides @Singleton static BehaviorSubject<Progress> progress() {
        return behavior(Progress.hide());
    }
    @Binds abstract Observable<String> uriObservableProvider(BehaviorSubject<String> impl);
    @Binds abstract Observer<String> uriObserverProvider(BehaviorSubject<String> impl);
    @Binds abstract Observable<Render> renderObservableProvider(BehaviorSubject<Render> impl);
    @Binds abstract Observer<Render> renderObserverProvider(BehaviorSubject<Render> impl);
    @Binds abstract Observable<Progress> progressObservableProvider(BehaviorSubject<Progress> impl);
    @Binds abstract Observer<Progress> progressObserverProvider(BehaviorSubject<Progress> impl);
}
