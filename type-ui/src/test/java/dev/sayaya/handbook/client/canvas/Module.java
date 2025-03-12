package dev.sayaya.handbook.client.canvas;

import dagger.Binds;
import dev.sayaya.handbook.client.interfaces.BoxElementList;
import dev.sayaya.handbook.client.repository.LanguageRepository;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;
import dev.sayaya.handbook.client.usecase.language.LanguageProvider;

@dagger.Module
public interface Module {
    @Binds LanguageProvider bindLanguageProvider(LanguageRepository impl);
    @Binds UpdatableBoxList updatableBoxProvider(BoxElementList impl);
}
