package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import dev.sayaya.handbook.interfaces.api.BrowserLanguageDetector;
import dev.sayaya.handbook.interfaces.api.FetchLanguagePackRepository;
import dev.sayaya.handbook.usecase.DocumentRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;

import javax.inject.Singleton;

/**
 * 테스트 환경을 위한 모의 API 통신 모듈.
 * FetchApi 대신 FetchMock을 제공한다.
 */
@Module
public interface MockApiModule {
    @Provides @Singleton static FetchApi fetch() { 
        return new FetchMock(); 
    }
    
    @Provides @Singleton static DocumentApi documentApi(FetchApi fetch) {
        return new DocumentApi(fetch);
    }

    @Binds @Singleton DocumentRepository bindDocumentRepository(DocumentApi impl);
    @Binds @Singleton LanguageDetector bindLanguageDetector(BrowserLanguageDetector impl);
    @Binds @Singleton LanguagePackRepository bindLanguagePackRepository(FetchLanguagePackRepository impl);
}
