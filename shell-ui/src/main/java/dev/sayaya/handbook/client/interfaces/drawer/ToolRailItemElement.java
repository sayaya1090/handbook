package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.ui.elements.IconElementBuilder;
import org.jboss.elemento.EventType;

public class ToolRailItemElement extends NavigationRailItemElement {
    @AssistedInject ToolRailItemElement(@Assisted Tool tool, ToolSelected selected) {
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon))
                .headline(tool.title);
        initEventHandlers(tool, selected);
        selected.subscribe(select->select(tool.equals(select)));
    }
    private void initEventHandlers(Tool tool, ToolSelected selected) {
        on(EventType.click, evt-> select(tool, selected));
    }
    private void select(Tool tool, ToolSelected selected) {
        selected.next(tool);
    }
}
