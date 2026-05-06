package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

/**
 * 개별 도구 버튼 요소.
 * 
 * <p><b>책임:</b> 도구 아이콘 및 라벨을 렌더링하고, 클릭 시 선택 상태를 변경한다.
 * 데스크톱 COLLAPSE 모드에서는 툴팁을 제공하며, 호버 탐색(peeking) 중에는 라벨이
 * 보이므로 툴팁을 비활성화한다.</p>
 */
public class ToolRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final Tool tool;
    private final TooltipCard tooltip;

    @AssistedInject 
    ToolRailItemElement(@Assisted Tool tool, ToolSelected selected, LabelProvider labelProvider, 
                        ToolRailMode toolRailMode, MenuHover hover) {
        this.tool = tool;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-solid", tool.icon(), "icon-filled"))
                .headline(headline.element());
        
        this.tooltip = TooltipCard.anchor(element()).position("end");
        
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(tool.title(), tool.title() != null ? tool.title() : "");
            // MenuRail 과 일관성을 위해 대문자 적용
            headline.element().innerHTML = title.toUpperCase();
            tooltip.content(title, null);
        });
        
        // 2026-05-05: peeking 중(MenuHover 에 값이 있음)에는 툴팁 비활성화 (라벨이 이미 보이거나 간섭 방지)
        toolRailMode.subscribe(state -> updateTooltip(state, hover.getValue()));
        hover.subscribe(h -> updateTooltip(toolRailMode.getValue(), h));

        if (tool.title() != null) element().dataset.set("toolTitle", tool.title());
        initEventHandlers(tool, selected, hover);
        selected.subscribe(select -> select(tool.equals(select)));
    }

    private void updateTooltip(ToolRailState state, Menu h) {
        // COLLAPSE 모드일 때만 툴팁 허용하되, 호버 탐색(peeking) 중에는 비활성화
        tooltip.enabled(state == ToolRailState.COLLAPSE && h == null);
    }

    private void initEventHandlers(Tool tool, ToolSelected selected, MenuHover hover) {
        on(EventType.click, evt -> {
            selected.next(tool);
            // 2026-05-05: 도구 선택 시 호버 상태를 명시적으로 해제하여 
            // ToolRail 이 즉시 COLLAPSE(아이콘만 노출) 되도록 유도.
            hover.next(null);
        });
    }
}
