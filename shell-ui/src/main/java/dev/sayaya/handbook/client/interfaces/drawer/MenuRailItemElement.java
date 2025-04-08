package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.ui.elements.IconElementBuilder;
import org.jboss.elemento.EventType;

public class MenuRailItemElement extends NavigationRailItemElement {
    @AssistedInject MenuRailItemElement(@Assisted Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement) {
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon))
                .headline(menu.title);
        if(menu.supportingText!=null) supportingText(menu.supportingText);
        if(menu.tools !=null && menu.tools.length > 1) trailingSupportingText("▶");
        initEventHandlers(menu, selected, hover, hoverElement);
        selected.subscribe(select->select(menu.equals(select)));
    }
    private void initEventHandlers(Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement) {
        on(EventType.click, evt-> select(menu, selected));
        on(EventType.mouseover, evt->{
            if(hover.getValue() == menu) return;
            hover.next(menu);
            hoverElement.next(this);
        });
    }
    private void select(Menu menu, MenuSelected selected) {
        selected.next(menu);
    }
}