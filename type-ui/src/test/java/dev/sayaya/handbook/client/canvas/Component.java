package dev.sayaya.handbook.client.canvas;

import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class })
public interface Component {
    CanvasElement canvas();
}
