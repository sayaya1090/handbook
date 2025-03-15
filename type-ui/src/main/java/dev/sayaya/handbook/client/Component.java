package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.repository.LanguageRepositoryModule;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { BoxElementModule.class, LanguageRepositoryModule.class })
public interface Component {
    CanvasElement canvas();
}
