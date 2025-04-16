package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ApiModule.class, ClientSharedModule.class })
public interface Component {
    ContentElement content();
    Observer<String> uri();
    Observer<Progress> progress();
    Observer<Render> renderer();
}