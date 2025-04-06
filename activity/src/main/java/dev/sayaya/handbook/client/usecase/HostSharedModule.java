package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Module
public class HostSharedModule {
    static {
        ClientWindow.progress = new ProgressSubject();
        ClientWindow.uri = new UriSubject();
        ClientWindow.renderer = new RendererSubject();
    }
    @Provides @Singleton static Observable<Progress> progress() { return ClientWindow.progress.asObservable(); }
    @Provides @Singleton static Observer<String> uri() { return ClientWindow.uri.subject; }
    @Provides @Singleton static Observable<Render> render() { return ClientWindow.renderer.asObservable(); }
}
