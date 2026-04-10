package dev.sayaya.handbook.client.interfaces.frame;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface FrameFactory {
    FrameElement frame();
}
