package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.TypeRepository;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds TypeRepository typeRepository(TypeApi impl);
    @Binds LayoutRepository layoutRepository(LayoutApi impl);
}
