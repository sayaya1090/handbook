package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class ContentElement implements IsElement<HTMLDivElement>, FrameContainer {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div;
    @Inject ContentElement(DrawerElement drawer) {
        this.div = div();
        div.id("content").style("display: flex; height: -webkit-fill-available; inset: 0;")
           .add(drawer);
    }
    @Override
    public ContentElement add(IsElement<?> element) {
        div.add(element);
        return this;
    }
}
