package dev.sayaya.handbook.client.repository;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.usecase.language.LanguageProvider;

@Module
public interface LanguageRepositoryModule {
    @Binds LanguageProvider bindLanguageProvider(LanguageRepository impl);
}
