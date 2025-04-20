package dev.sayaya.handbook.client.create;

import dev.sayaya.handbook.client.interfaces.create.ContentElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.LanguagePackManager;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, ClientSharedModule.class })
public interface Component {
    LanguagePackManager languagePackManager();
    ContentElement contentElement();
}