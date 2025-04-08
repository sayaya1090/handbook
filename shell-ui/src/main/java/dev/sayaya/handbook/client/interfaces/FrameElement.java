package dev.sayaya.handbook.client.interfaces;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class FrameElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("frame");
    public FrameElement() {
        css("frame-in");
    }
    static void fadeOut(HTMLElement elem) {
        elem.classList.add("frame-out");
    }
    static void fadeIn(HTMLElement elem) {
        elem.classList.remove("frame-in");
    }
}
