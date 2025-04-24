package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.api.ApiModule;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { BoxElementModule.class, ApiModule.class, ClientSharedModule.class })
public interface Component {
    CanvasElement canvas();
    Observer<String> uri();
    Observer<Progress> progress();
    Observer<Render> renderer();
    Observable<Tool[]> tools();
}