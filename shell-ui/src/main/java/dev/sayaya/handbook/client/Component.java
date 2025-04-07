package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.AppBarElement;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class })
public interface Component {
    AppBarElement appBar();
    Observable<Progress> progress();
    Observer<String> uri();
    Observable<Render> renderer();
}
