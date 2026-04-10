package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class MenuRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final HTMLContainerBuilder<HTMLDivElement> supportingText = div();
    private final Menu menu;
    @AssistedInject MenuRailItemElement(@Assisted Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement, LabelProvider labelProvider) {
        this.menu = menu;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon()))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon()))
                .headline(headline.element()).supportingText(supportingText.element());
        if(menu.tools() != null && menu.tools().length > 1) trailingSupportingText("\u25B6");
        labelProvider.subscribe(labels -> {
            headline.element().innerHTML = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "").toUpperCase();
            supportingText.element().innerHTML = labels.getOrDefault(menu.supportingText(), menu.supportingText() != null ? menu.supportingText() : "");
        });
        initEventHandlers(menu, selected, hover, hoverElement);
        selected.subscribe(select -> select(menu.equals(select)));
    }
    private void initEventHandlers(Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement) {
        on(EventType.click, evt -> select(menu, selected));
        on(EventType.mouseover, evt -> {
            if(hover.getValue() == menu) return;
            hover.next(menu);
            hoverElement.next(this);
        });
    }
    private void select(Menu menu, MenuSelected selected) {
        selected.next(menu);
    }
}
