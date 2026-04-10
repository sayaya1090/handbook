package dev.sayaya.handbook.client.interfaces.api;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.FetchApi;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
}
