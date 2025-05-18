package dev.sayaya.handbook.client.tab;

import dagger.Binds;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.handbook.client.usecase.LanguageProvider;

@dagger.Module
public abstract class MockModule {
    @Binds abstract LanguageProvider bindLanguageProvider(LanguageRepository impl);
}
