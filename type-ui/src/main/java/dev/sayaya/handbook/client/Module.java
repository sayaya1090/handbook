package dev.sayaya.handbook.client;

import dagger.Binds;
import dev.sayaya.handbook.client.repository.LanguageRepository;
import dev.sayaya.handbook.client.usecase.language.LanguageProvider;

@dagger.Module
public interface Module {
    @Binds LanguageProvider bindLanguageProvider(LanguageRepository impl);
}
