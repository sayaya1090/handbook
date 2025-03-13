package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
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

import static org.jboss.elemento.Elements.div;

@Singleton
public class BoxContextMenuElement implements IsElement<MdMenuElement> {
    @Delegate  private final MenuElementBuilder.TopMenuElementBuilder menu = MenuElementBuilder.menu().positioning(MenuElementBuilder.Position.Popover);
    private final MenuElementBuilder.MenuItemElementBuilder<?> delType;
    @Inject BoxContextMenuElement(ActionManager actions, SelectedBoxElement selected, LanguagePackManager labels) {
        this(div(), actions, selected, labels);
    }
    private BoxContextMenuElement(HTMLContainerBuilder<HTMLDivElement> container, ActionManager actions, SelectedBoxElement selected, LanguagePackManager labels) {
        container.add(menu.anchorElement(container));
        delType = menu.item().headline("Delete Type");
        // menu.add(divider());
        menu.element().stayOpenOnFocusout = true;
        menu.element().stayOpenOnOutsideClick = true;
        on(EventType.click, Event::stopPropagation);        // Canvas의 Click 호출을 차단한다
        labels.subscribe(this::update);

        delType.on(EventType.click, evt->actions.delType(selected.getValue().box()));
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
}
