package dev.sayaya.handbook.client.interfaces.controller;

import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

@Singleton
public class ControllerElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @Inject ControllerElement(
            AddButton add, RemoveButton remove,
            UndoButton undo, RedoButton redo, SaveButton save,
            NameElement name, BeforeButton before, VersionElement version, AfterButton after,
            EffectDatetimeElement effectDatetime, ExpireDatetimeElement expireDatetime
    ) {
        css("controller")
                .add(div().style("flex: 1 1 0%; display:flex; align-items:center; justify-content: flex-start; gap:0.5rem;").add(add).add(remove).add(undo).add(redo))
                .add(div().style("flex: 1 1 0%; display:flex; align-items:center; justify-content: center; gap:0.5rem;").add(before).add(version).add(after))
                .add(div().style("flex: 1 1 0%; display:flex; align-items:center; justify-content: flex-end; gap:0.5rem;").add(effectDatetime).add(label("~")).add(expireDatetime).add(save));
    }
}