package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.client.usecase.action.SaveAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SaveButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    SaveButton(TypeRepository typeRepository, LayoutRepository layoutRepository,
               TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
               ActionManager actionManager, LayoutProvider layoutProvider,
               LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().filled()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-floppy-disk"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e ->
                new SaveAction(typeRepository, layoutRepository, typeList, positionMap,
                        tracker, actionManager, layoutProvider).execute()
        );

        labelProvider.subscribe(labels ->
                root.textContent = labels.getOrDefault("type.save", "Save"));
    }

    @Override
    public HTMLElement element() { return root; }
}
