package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.ui.ContentElement;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { LoginModule.class, ApiModule.class })
public interface Component {
    ContentElement content();
    Observer<Render> renderer();
    OAuthApi api();
}
