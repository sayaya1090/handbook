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
    @Inject ControllerElement(ReloadButton reload, AddTypeButton add, RemoveTypeButton remove, UndoButton undo, RedoButton redo, DocumentButton doc, SaveButton save) {
        css("controller")
                .add(reload)
                .add(div().style("display:flex; gap:0.5rem;").add(add).add(remove))
                .add(div().style("display:flex; gap:0.5rem;").add(undo).add(redo))
                .add(doc)
                .add(save);
    }
}
