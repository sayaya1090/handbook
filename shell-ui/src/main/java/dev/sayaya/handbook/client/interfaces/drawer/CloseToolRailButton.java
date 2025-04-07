package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.Event;
import org.jboss.elemento.EventType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CloseToolRailButton extends NavigationRailItemElement {
    private final MenuRailMode menu;
    private final ToolRailMode tools;
    @Inject CloseToolRailButton(MenuRailMode menu, ToolRailMode tools) {
        this.menu = menu;
        this.tools = tools;

        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-left"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-left"));
        element().style.marginTop = CSSProperties.MarginTopUnionType.of("auto");

        on(EventType.click, this::closeToolRail);

    }
    private void closeToolRail(Event evt) {
        evt.preventDefault();
        menu.next(MenuRailState.COLLAPSE);
        tools.next(ToolRailState.HIDE);
    }
}
