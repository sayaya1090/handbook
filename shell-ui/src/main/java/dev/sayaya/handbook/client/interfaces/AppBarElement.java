package dev.sayaya.handbook.client.interfaces;

import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static org.jboss.elemento.Elements.header;

@Singleton
public class AppBarElement extends HTMLContainerBuilder<HTMLElement> implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLElement> _this;
    @Inject AppBarElement(ProgressElement progress) {
        this(header(), progress);
    }
    private AppBarElement(HTMLContainerBuilder<HTMLElement> element, ProgressElement progress) {
        super(element.element());
        _this = element;
        var menu = button().icon().add(icon("menu"));
        element.add(progress.element()).add(menu.style("position: relative; z-index: 10;"));
        // menu.on(EventType.click, evt->isMenuShown.next(!isMenuShown.getValue()));
    }
}