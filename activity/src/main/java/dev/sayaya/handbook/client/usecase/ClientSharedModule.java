package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Module
public class ClientSharedModule {
    @Provides @Singleton static Observer<Progress> progress() { return ClientWindow.progress; }
    @Provides @Singleton static Observer<String> uri() { return ClientWindow.uri; }
    @Provides @Singleton static Observer<Render> render() { return ClientWindow.renderer; }
    @Provides @Singleton static Observable<Tool[]> tools() { return ClientWindow.tools.asObservable(); }
    @Provides @Singleton static Observable<Label> labels() { return ClientWindow.labels; }
    @Provides @Singleton static Observable<Workspace> workspace() { return ClientWindow.workspace; }
}
