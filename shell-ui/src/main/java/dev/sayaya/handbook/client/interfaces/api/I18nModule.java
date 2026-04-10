package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;

@Module
public interface I18nModule {
    @Binds LanguageDetector languageDetector(BrowserLanguageDetector impl);
    @Binds LanguagePackRepository languagePackRepository(FetchLanguagePackRepository impl);
}
