package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;

/** Undo 버튼. */
@Singleton
public class UndoButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public UndoButton(ActionManager actionManager, LabelProvider labelProvider) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-undo").element();
        labelProvider.subscribe(labels -> element.textContent = labels.getOrDefault("document.undo", "Undo"));
        element.addEventListener("click", e -> actionManager.undo());
        actionManager.onCanUndo(can ->
            ((elemental2.dom.HTMLButtonElement) element).disabled = !can
        );
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
