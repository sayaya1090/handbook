package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class ToolRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final Tool tool;
    @AssistedInject ToolRailItemElement(@Assisted Tool tool, ToolSelected selected, LabelProvider labelProvider) {
        this.tool = tool;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .headline(headline.element());
        labelProvider.subscribe(labels -> {
            headline.element().innerHTML = labels.getOrDefault(tool.title(), tool.title() != null ? tool.title() : "");
        });
        initEventHandlers(tool, selected);
        selected.subscribe(select -> select(tool.equals(select)));
    }
    private void initEventHandlers(Tool tool, ToolSelected selected) {
        on(EventType.click, evt -> select(tool, selected));
    }
    private void select(Tool tool, ToolSelected selected) {
        selected.next(tool);
    }
}
