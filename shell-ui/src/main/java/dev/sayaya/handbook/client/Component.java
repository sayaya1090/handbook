package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ModuleScriptElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class, ApiModule.class, HostSharedModule.class })
public interface Component {
    ModuleScriptElement scriptElement();
    ProgressElement progressElement();
    ContentElement contentElement();
    Observable<Progress> progress();
    Observer<String> uri();
    Observable<Render> renderer();
}
