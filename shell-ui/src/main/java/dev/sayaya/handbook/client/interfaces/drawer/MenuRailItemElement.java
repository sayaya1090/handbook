package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.SessionState;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.SessionStateProvider;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.SessionStateKind;
import dev.sayaya.handbook.usecase.LabelProvider;

import java.util.List;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
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
    private final MenuList menuList;
    private final MenuSelected selected;
    // isAllowedFor=false 일 때 클릭 시 리라우팅할 대체 메뉴 (CTA). null 이면 무반응.
    private Menu ctaFallback = null;

    @AssistedInject MenuRailItemElement(@Assisted Menu menu, MenuSelected selected,
                                        MenuHover hover,
                                        MenuSelectedElementProvider selectedElement,
                                        MenuRailMode menuRailMode, LabelProvider labelProvider,
                                        SessionStateProvider sessionStateProvider,
                                        MenuList menuList) {
        this.menu = menu;
        this.menuRailMode = menuRailMode;
        this.menuList = menuList;
        this.selected = selected;
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

        // 세션 상태에 따라 가시성/활성/CTA 폴백을 재계산. menuList 변동 시에도 재평가.
        sessionStateProvider.subscribe(this::applyVisibility);
        menuList.subscribe(list -> applyVisibility(sessionStateProvider.getValue()));

        initEventHandlers(menu, selected, hover, selectedElement);
        // agent-command highlight 가 .ui-highlight class 를 부여하면 tooltip 즉시 표시.
        // MutationObserver 세부는 HighlightEffect 로 캡슐화되어 있다 (Dependency Inversion).
        HighlightEffect.observe(element(), () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        selected.subscribe(select -> select(menu.equals(select)));
    }

    /**
     * 현재 세션 상태 기준으로 이 메뉴가 허용되는지 평가해 {@code [disabled]} 속성과 CTA 폴백을 갱신한다.
     *
     * <p>허용 = {@code element().removeAttribute("disabled")} + {@code ctaFallback=null}.
     * 비허용 = {@code [disabled]} 속성 + opacity/pointer-events 는 CSS 가 처리.
     * 클릭은 이벤트 핸들러가 살아있되 분기 처리 — {@link #initEventHandlers} 참조.</p>
     */
    private void applyVisibility(SessionState state) {
        if (state == null) return;
        boolean allowed = menu.isAllowedFor(state.kind());
        if (allowed) {
            element().removeAttribute("disabled");
            element().classList.remove("menu-disabled");
            ctaFallback = null;
        } else {
            element().setAttribute("disabled", "");
            element().classList.add("menu-disabled");
            ctaFallback = resolveCta(state.kind());
        }
    }

    /**
     * 현재 상태에서 허용되는 메뉴 중 첫 번째를 폴백으로 선택.
     * ANONYMOUS → Sign In / AUTHENTICATED → 워크스페이스 생성 등 자연스러운 상위 상태 유도.
     * 없으면 null — 클릭 시 무반응.
     */
    private Menu resolveCta(SessionStateKind kind) {
        List<Menu> all = menuList.getValue();
        if (all == null) return null;
        for (Menu m : all) {
            if (m == menu) continue;
            if (m.script() == null) continue;
            if (!m.isAllowedFor(kind)) continue;
            return m;
        }
        return null;
    }

    private void initEventHandlers(Menu menu, MenuSelected selected, MenuHover hover,
                                   MenuSelectedElementProvider selectedElement) {
        on(EventType.click, evt -> {
            if (element().hasAttribute("disabled")) {
                // 비허용 상태에서는 CTA 폴백이 있으면 그 쪽으로 라우팅 (Sign In / 워크스페이스 생성 등),
                // 없으면 클릭 무시 — 2026-04-18 "WS 있을 때 강제 진입" 회귀 회피.
                if (ctaFallback != null) {
                    selected.next(ctaFallback);
                    selectedElement.next(this);
                }
                return;
            }
            selected.next(menu);
            selectedElement.next(this);
        });
        on(EventType.mouseover, evt -> {
            if (element().hasAttribute("disabled")) return;
            // EXPAND 에서만 hover peek. COLLAPSE/모바일에선 TooltipCard 가 라벨만 표시.
            if (menuRailMode.getValue() != MenuRailState.EXPAND) return;
            if (hover.getValue() == menu) return;
            hover.next(menu);
            selectedElement.next(this);
        });
    }

    private void select(Menu menu, MenuSelected selected) {
        selected.next(menu);
    }
}
