package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class SharedModule {
    @Provides @Singleton static ProgressSubject progress() { return ClientWindow.progress; }
    @Provides @Singleton static UriSubject uri() { return ClientWindow.uri; }
    @Provides @Singleton static RendererSubject render() { return ClientWindow.renderer; }
}
