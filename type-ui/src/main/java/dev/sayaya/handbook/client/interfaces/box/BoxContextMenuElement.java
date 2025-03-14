package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.language.LanguagePackManager;
import dev.sayaya.ui.dom.MdMenuElement;
import dev.sayaya.ui.elements.MenuElementBuilder;
import elemental2.dom.*;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class BoxContextMenuElement implements IsElement<MdMenuElement> {
    @Delegate  private final MenuElementBuilder.TopMenuElementBuilder menu = MenuElementBuilder.menu().positioning(MenuElementBuilder.Position.Popover);
    private final MenuElementBuilder.MenuItemElementBuilder<?> delType;
    private final ActionManager actionManager;
    private final SelectedBoxElement selected;
    @Inject BoxContextMenuElement(ActionManager actions, SelectedBoxElement selected, LanguagePackManager labels) {
        this(div(), actions, selected, labels);
    }
    private BoxContextMenuElement(HTMLContainerBuilder<HTMLDivElement> container, ActionManager actions, SelectedBoxElement selected, LanguagePackManager labels) {
        container.add(menu.anchorElement(container));
        this.actionManager = actions;
        this.selected = selected;
        delType = menu.item().headline("Delete Type");
        // menu.add(divider());
        menu.element().stayOpenOnFocusout = true;
        menu.element().stayOpenOnOutsideClick = true;
        on(EventType.click, Event::stopPropagation);        // Canvas의 Click 호출을 차단한다
        labels.subscribe(this::update);

        delType.on(EventType.click, this::handeDelete);
    }
    private void update(Label labels) {
        updateLabel(delType, labels.delType());
    }
    private static void updateLabel(MenuElementBuilder.MenuItemElementBuilder<?> item, String label) {
        item.element().childNodes.asList().stream()
                .filter(s-> s.attributes.get("slot").value.equals("headline"))
                .map(s->(HTMLElement)s)
                .forEach(Element::remove);
        item.headline(label);
    }
    private void handeDelete(MouseEvent evt) {
        var targetBoxes = selected.getValue().stream().map(BoxElement::box).toArray(Box[]::new);
        actionManager.delType(targetBoxes);
    }
}
