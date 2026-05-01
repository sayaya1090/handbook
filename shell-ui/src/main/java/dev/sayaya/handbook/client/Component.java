package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ShellModule.class, ApiModule.class })
public interface Component {
    ShellInitializer shellInitializer();
}
