package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.client.usecase.TypeRepository;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds LayoutRepository layoutRepositoryProvider(LayoutApi impl);
    @Binds TypeRepository typeRepositoryProvider(TypeApi impl);
}
