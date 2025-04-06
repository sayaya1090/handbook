package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Module
public class ClientSharedModule {
    @Provides @Singleton static Observer<Progress> progress() { return ClientWindow.progress.subject; }
    @Provides @Singleton static UriSubject uri() { return ClientWindow.uri; }
    @Provides @Singleton static Observer<Render> render() { return ClientWindow.renderer.subject; }
}
