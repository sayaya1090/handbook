package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.api.I18nModule;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class, ApiModule.class, I18nModule.class, HostSharedModule.class })
public interface Component {
    ShellInitializer shellInitializer();
}
