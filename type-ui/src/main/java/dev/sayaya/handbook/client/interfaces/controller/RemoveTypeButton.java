package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class RemoveTypeButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    RemoveTypeButton(ActionManager actionManager, TypeList typeList, ChangeTracker tracker,
                     SelectedBoxElement selection, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().outlined()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-trash"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e -> {
            Set<String> selected = selection.getValue();
            for (TypeValue type : typeList.getValue()) {
                if (selected.contains(type.key())) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
            }
        });

        labelProvider.subscribe(labels ->
                root.textContent = labels.getOrDefault("type.remove", "Remove"));
    }

    @Override
    public HTMLElement element() { return root; }
}
