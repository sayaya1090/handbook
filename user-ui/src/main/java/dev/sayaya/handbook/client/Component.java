package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ClientSharedModule.class })
public interface Component {
    Observer<String> uri();
    Observer<Progress> progress();
    Observer<Render> renderer();
}