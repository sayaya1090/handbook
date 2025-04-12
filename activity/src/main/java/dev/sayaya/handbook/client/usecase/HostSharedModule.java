package dev.sayaya.handbook.client.usecase;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static elemental2.dom.DomGlobal.window;

@Module
public abstract class HostSharedModule {
    static {
        ClientWindow.progress = behavior(new Progress());
        ClientWindow.uri = behavior(window.location.href);
        ClientWindow.renderer = behavior(null);
        ClientWindow.tools = behavior(new Tool[]{});
        ClientWindow.labels = behavior(null);
    }
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return ClientWindow.progress; }
    @Provides @Singleton static BehaviorSubject<String> uri() { return ClientWindow.uri; }
    @Provides @Singleton static Observable<Render> render() { return ClientWindow.renderer.asObservable(); }
    @Provides @Singleton static Observer<Tool[]> tools() { return ClientWindow.tools; }
    @Provides @Singleton static BehaviorSubject<Label> labels() { return ClientWindow.labels; }
    @Binds abstract Observable<Progress> progressObservableProvider(BehaviorSubject<Progress> impl);
    @Binds abstract Observer<Progress> progressObserverProvider(BehaviorSubject<Progress> impl);
    @Binds abstract Observable<String> uriObservableProvider(BehaviorSubject<String> impl);
    @Binds abstract Observer<String> uriObserverProvider(BehaviorSubject<String> impl);
    @Binds abstract LanguageProvider bindLanguageProvider(LanguageRepository impl);
}
