package dev.sayaya.handbook.client.interfaces.controller;

import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class ControllerElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @Inject ControllerElement(ReloadButton reload, SaveButton save) {
        add(reload).add(save);
    }
}
