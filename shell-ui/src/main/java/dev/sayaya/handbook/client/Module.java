package dev.sayaya.handbook.client;

import dagger.Binds;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;

@dagger.Module
public interface Module {
    @Binds FrameContainer frameContainerProvider(ContentElement impl);
}
