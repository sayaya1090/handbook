package dev.sayaya.handbook.client.frame;

import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { FrameMock.class })
public interface Component {
    FrameContainerImpl container();
    FrameUpdater updater();
    @Named("renderer1") Render renderer1();
    @Named("renderer2") Render renderer2();
    Observer<Render> render();
}
