package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.ProgressStore;
import dev.sayaya.handbook.client.usecase.RenderStore;
import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Module
public interface StateModule {
    @Provides @Singleton static UriStore uriStore() { return new UriStore(); }
    @Binds Observable<String> uriObservable(UriStore store);
    @Binds Observer<String> uriObserver(UriStore store);

    @Provides @Singleton static ProgressStore progressStore() { return new ProgressStore(); }
    @Binds Observable<Progress> progressObservable(ProgressStore store);
    @Binds Observer<Progress> progressObserver(ProgressStore store);

    @Provides @Singleton static RenderStore renderStore() { return new RenderStore(); }
    @Binds Observable<Render> renderObservable(RenderStore store);
    @Binds Observer<Render> renderObserver(RenderStore store);
}
