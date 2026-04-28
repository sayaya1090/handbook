package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class ToolRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final Tool tool;
    private final TooltipCard tooltip;
    @AssistedInject ToolRailItemElement(@Assisted Tool tool, ToolSelected selected, LabelProvider labelProvider, ToolRailMode toolRailMode) {
        this.tool = tool;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .headline(headline.element());
        this.tooltip = TooltipCard.anchor(element()).position("end");
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(tool.title(), tool.title() != null ? tool.title() : "");
            headline.element().innerHTML = title;
            tooltip.content(title, null);
        });
        // ToolRail EXPAND 에서는 headline 이 이미 보이므로 tooltip 중복 노출 방지.
        toolRailMode.subscribe(state -> tooltip.enabled(state == ToolRailState.COLLAPSE));
        if (tool.title() != null) element().dataset.set("toolTitle", tool.title());
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
