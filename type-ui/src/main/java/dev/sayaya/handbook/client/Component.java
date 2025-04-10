package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.repository.LanguageRepositoryModule;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.handbook.client.usecase.UriSubject;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { BoxElementModule.class, LanguageRepositoryModule.class, ApiModule.class, ClientSharedModule.class })
public interface Component {
    CanvasElement canvas();
    UriSubject uri();
    Observer<Render> renderer();
}
