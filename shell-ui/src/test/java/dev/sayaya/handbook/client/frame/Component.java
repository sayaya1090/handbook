package dev.sayaya.handbook.client.frame;

import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.Render;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, FrameMock.class })
public interface Component {
    FrameContainerImpl container();
    FrameUpdater updater();
    @Named("renderer1")  Render renderer1();
    @Named("renderer2")  Render renderer2();
}
