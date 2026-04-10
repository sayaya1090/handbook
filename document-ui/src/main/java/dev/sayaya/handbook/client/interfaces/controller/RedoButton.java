package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;

/** Redo 버튼. */
@Singleton
public class RedoButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public RedoButton(ActionManager actionManager) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-redo").element();
        element.textContent = "Redo";
        element.addEventListener("click", e -> actionManager.redo());
        actionManager.onCanRedo(can ->
            ((elemental2.dom.HTMLButtonElement) element).disabled = !can
        );
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
