package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.create.ContentElement;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { WorkspaceModule.class, ApiModule.class })
public interface Component {
    ContentElement contentElement();
}
