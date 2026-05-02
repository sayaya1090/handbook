package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.interfaces.api.BrowserLanguageDetector;
import dev.sayaya.handbook.interfaces.api.FetchLanguagePackRepository;
import dev.sayaya.handbook.usecase.*;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;

/**
 * 운영 환경을 위한 실제 HTTP API 통신을 담당하는 모듈.
 */
@Module
public interface ProductionApiModule {
    @Provides static FetchApi fetch() { 
        return (url, param) -> DomGlobal.window.fetch(url, param); 
    }
    @Binds @Singleton DocumentRepository bindDocumentRepository(DocumentApi impl);
    @Binds @Singleton LanguageDetector bindLanguageDetector(BrowserLanguageDetector impl);
    @Binds @Singleton LanguagePackRepository bindLanguagePackRepository(FetchLanguagePackRepository impl);
}
