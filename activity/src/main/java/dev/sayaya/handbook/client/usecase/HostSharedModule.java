package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;
import java.util.List;

@Module
public class HostSharedModule {
    static {
        ClientWindow.progress = new ProgressSubject();
        ClientWindow.uri = new UriSubject();
        ClientWindow.renderer = new RendererSubject();
        ClientWindow.tools = new ToolSubject();
    }
    @Provides @Singleton static Observable<Progress> progress() { return ClientWindow.progress.subject.asObservable(); }
    @Provides @Singleton static Observable<String> uri() { return ClientWindow.uri.subject.asObservable(); }
    @Provides @Singleton static Observable<Render> render() { return ClientWindow.renderer.subject.asObservable(); }
    @Provides @Singleton static Observer<List<Tool>> tools() { return ClientWindow.tools.subject; }

}
