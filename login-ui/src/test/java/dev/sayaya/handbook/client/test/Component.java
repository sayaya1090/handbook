package dev.sayaya.handbook.client.test;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Log;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ClientSharedModule.class, HostMockModule.class })
public interface Component {
    ContentElement content();
    Log log();
    Observer<String> uri();
    Observer<Progress> progress();
    Observer<Render> renderer();
}