package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.language.LanguagePackManager;
import dev.sayaya.ui.dom.MdMenuElement;
import dev.sayaya.ui.elements.MenuElementBuilder;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.DividerElementBuilder.divider;
import static org.jboss.elemento.Elements.div;

@Singleton
public class CanvasContextMenuElement implements IsElement<MdMenuElement> {
    @Delegate  private final MenuElementBuilder.TopMenuElementBuilder menu = MenuElementBuilder.menu().positioning(MenuElementBuilder.Position.Popover);
    private final MenuElementBuilder.MenuItemElementBuilder<?> addType;
    private final MenuElementBuilder.MenuItemElementBuilder<?> undo;
    private final MenuElementBuilder.MenuItemElementBuilder<?> redo;
    private final MenuElementBuilder.MenuItemElementBuilder<?> search;
    @Inject CanvasContextMenuElement(ActionManager actions, LanguagePackManager labels) {
        this(div(), actions, labels);
    }
    private CanvasContextMenuElement(HTMLContainerBuilder<HTMLDivElement> container, ActionManager actions, LanguagePackManager labels) {
        container.add(menu.anchorElement(container));
        addType = menu.item().headline("Add Type");
        menu.add(divider());
        undo = menu.item().headline("Undo");
        redo = menu.item().headline("Redo");
        search = menu.item().headline("Search");
        menu.element().stayOpenOnFocusout = true;
        menu.element().stayOpenOnOutsideClick = true;
        on(EventType.click, Event::stopPropagation);        // Canvas의 Click 호출을 차단한다

        addType.on(EventType.click, evt->actions.addType(element().xOffset, element().yOffset));
        undo.on(EventType.click, evt->actions.undo());
        redo.on(EventType.click, evt->actions.redo());
        search.on(EventType.click, evt->actions.search());

        labels.subscribe(this::update);
    }
    private void update(Label labels) {
        updateLabel(addType, labels.addType());
        updateLabel(undo, labels.undo());
        updateLabel(redo, labels.redo());
    }
    private static void updateLabel(MenuElementBuilder.MenuItemElementBuilder<?> item, String label) {
        item.element().childNodes.asList().stream()
                .filter(s-> s.attributes.get("slot").value.equals("headline"))
                .map(s->(HTMLElement)s)
                .forEach(Element::remove);
        item.headline(label);
    }
}
