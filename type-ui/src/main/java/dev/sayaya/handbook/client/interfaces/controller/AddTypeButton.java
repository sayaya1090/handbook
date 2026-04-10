package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.ContextMenuHelper;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AddTypeButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    AddTypeButton(ActionManager actionManager, TypeList typeList, PositionMap positionMap,
                  ChangeTracker tracker, LayoutProvider layoutProvider, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().filled()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-plus"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e -> {
            LayoutPeriod period = layoutProvider.getValue();
            if (period == null) return;
            String id = ContextMenuHelper.uniqueTypeId(typeList);
            TypeValue newType = TypeValue.create(id, "1.0", period.effectDateTime, period.expireDateTime);
            Position pos = Position.of(50, 80, 240, 160);
            actionManager.execute(new ComplexAction(
                    new CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                    new PushOutOverlapAction(positionMap, newType.key(), 10)
            ));
        });

        labelProvider.subscribe(labels ->
                root.textContent = labels.getOrDefault("type.add", "Add"));
    }

    @Override
    public HTMLElement element() { return root; }
}
