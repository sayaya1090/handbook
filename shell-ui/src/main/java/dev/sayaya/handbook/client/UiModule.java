package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;

@Module
public interface UiModule {
    @Binds FrameContainer frameContainer(ContentElement impl);
}
