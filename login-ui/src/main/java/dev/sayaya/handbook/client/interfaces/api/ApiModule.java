package dev.sayaya.handbook.client.interfaces.api;

import dagger.Module;
import dagger.Provides;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
}
