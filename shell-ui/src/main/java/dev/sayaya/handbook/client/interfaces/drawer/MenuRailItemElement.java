package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.MutationRecord;
import elemental2.core.JsArray;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

/**
 * MenuRail 의 개별 메뉴 아이템.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>메뉴 아이콘·headline·supportingText 렌더 (LabelProvider 로 i18n)</li>
 *   <li>click 시 {@link MenuSelected} 및 {@link MenuSelectedElementProvider} 발행</li>
 *   <li>MenuRail EXPAND 상태에서 hover 시 {@link MenuHover} 발행 → ToolRail peek (기존 UC-S6 탐색 UX 유지)</li>
 *   <li>MenuRail COLLAPSE/모바일 상태에서 hover 시 {@link TooltipCard} 로 메뉴명 표시 (ToolRail 자동 전환 없음)</li>
 *   <li>{@code .ui-highlight} class 가 부여되면(agent-command) tooltip 을 동반 표시하여 시각 유도 + 라벨 안내</li>
 * </ul></p>
 *
 * <p><b>UC-S6 2026-04-17 재정의:</b> 과거 상태 무관 hover 전환은 CloseToolRailButton
 * 복귀 의사를 무시했음. 이제 **EXPAND 에서만 peek** 허용 — COLLAPSE/모바일은 tooltip 만.</p>
 */
public class MenuRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final HTMLContainerBuilder<HTMLDivElement> supportingText = div();
    private final Menu menu;
    private final TooltipCard tooltip;
    private final MenuRailMode menuRailMode;

    @AssistedInject MenuRailItemElement(@Assisted Menu menu, MenuSelected selected,
                                        MenuHover hover,
                                        MenuSelectedElementProvider selectedElement,
                                        MenuRailMode menuRailMode, LabelProvider labelProvider) {
        this.menu = menu;
        this.menuRailMode = menuRailMode;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon(), "icon-outline"))
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", menu.icon(), "icon-filled"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon(), "icon-outline"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-solid", menu.icon(), "icon-filled"))
                .headline(headline.element()).supportingText(supportingText.element());
        if(menu.tools() != null && menu.tools().length > 1) trailingSupportingText("\u25B6");

        this.tooltip = TooltipCard.anchor(element()).position("end");

        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            String sub = labels.getOrDefault(menu.supportingText(),
                    menu.supportingText() != null ? menu.supportingText() : "");
            headline.element().innerHTML = title.toUpperCase();
            supportingText.element().innerHTML = sub;
            tooltip.content(title, sub);
        });

        // Tooltip 은 COLLAPSE 에서만 활성. EXPAND 에선 headline 이 이미 보이고 hover peek 가 ToolRail 을 연다.
        menuRailMode.subscribe(state -> tooltip.enabled(state == MenuRailState.COLLAPSE));

        initEventHandlers(menu, selected, hover, selectedElement);
        observeHighlight();
        selected.subscribe(select -> select(menu.equals(select)));
    }

    private void initEventHandlers(Menu menu, MenuSelected selected, MenuHover hover,
                                   MenuSelectedElementProvider selectedElement) {
        on(EventType.click, evt -> {
            selected.next(menu);
            selectedElement.next(this);
        });
        on(EventType.mouseover, evt -> {
            // EXPAND 에서만 hover peek. COLLAPSE/모바일에선 TooltipCard 가 라벨만 표시.
            if (menuRailMode.getValue() != MenuRailState.EXPAND) return;
            if (hover.getValue() == menu) return;
            hover.next(menu);
            selectedElement.next(this);
        });
    }

    /**
     * agent-command highlight 가 {@code .ui-highlight} class 를 부여하면 tooltip 을
     * 즉시 표시(3초 자동 종료)해 시각 유도 + 라벨 안내를 함께 제공한다.
     *
     * <p>MenuRailMode 가 EXPAND 일 때는 tooltip.enabled=false 라 showImmediate 도 no-op
     * 이 되어 중복 노출이 발생하지 않는다 (EXPAND 에서는 headline 이 이미 보임).</p>
     */
    private void observeHighlight() {
        MutationObserver observer = new MutationObserver((JsArray<MutationRecord> records, MutationObserver o) -> {
            for (int i = 0; i < records.length; i++) {
                MutationRecord r = records.getAt(i);
                if ("class".equals(r.attributeName) && element().classList.contains("ui-highlight")) {
                    tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS);
                }
            }
            return null;
        });
        MutationObserverInit init = MutationObserverInit.create();
        init.setAttributes(true);
        init.setAttributeFilter(JsArray.of("class"));
        observer.observe(element(), init);
    }

    private void select(Menu menu, MenuSelected selected) {
        selected.next(menu);
    }
}
