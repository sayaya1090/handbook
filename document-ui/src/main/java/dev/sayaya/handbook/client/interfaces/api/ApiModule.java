package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.DocumentRepository;
import dev.sayaya.handbook.usecase.FetchApi;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds DocumentRepository bindDocumentRepository(DocumentApi impl);
}
