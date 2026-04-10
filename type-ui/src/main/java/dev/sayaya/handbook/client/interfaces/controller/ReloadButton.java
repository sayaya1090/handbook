package dev.sayaya.handbook.client.interfaces.controller;


import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReloadButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    ReloadButton(TypeRepository typeRepository, LayoutRepository layoutRepository,
                 TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                 ActionManager actionManager, LayoutProvider layoutProvider, LayoutList layoutList,
                 LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e ->
                new LoadAction(typeRepository, layoutRepository, typeList, positionMap,
                        tracker, actionManager, layoutProvider, layoutList).execute()
        );

        labelProvider.subscribe(labels ->
                root.title = labels.getOrDefault("type.reload", "Reload"));
    }

    @Override
    public HTMLElement element() { return root; }
}
