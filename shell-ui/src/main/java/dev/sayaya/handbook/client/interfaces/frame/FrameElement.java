package dev.sayaya.handbook.client.interfaces.frame;

import dagger.assisted.AssistedInject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class FrameElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("frame");
    @AssistedInject FrameElement() {
        css("frame-in");
    }
    void fadeOut() {
        element().classList.add("frame-out");
    }
    void fadeIn() {
        element().classList.remove("frame-in");
    }
}