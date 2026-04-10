package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RedoButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    RedoButton(ActionManager actionManager, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate-right"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e -> actionManager.redo());
        actionManager.onCanRedo(can -> root.toggleAttribute("disabled", !can));

        labelProvider.subscribe(labels ->
                root.title = labels.getOrDefault("type.redo", "Redo"));
    }

    @Override
    public HTMLElement element() { return root; }
}
