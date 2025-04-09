package dev.sayaya.handbook.client.interfaces.frame;

import org.jboss.elemento.IsElement;

public interface FrameContainer {
    FrameContainer add(IsElement<?> element);
}