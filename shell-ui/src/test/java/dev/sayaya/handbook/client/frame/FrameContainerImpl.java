package dev.sayaya.handbook.client.frame;

import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class FrameContainerImpl implements FrameContainer, IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @Inject FrameContainerImpl() {}
    @Override
    public FrameContainerImpl add(IsElement<?> element) {
        container.add(element);
        return this;
    }
}
