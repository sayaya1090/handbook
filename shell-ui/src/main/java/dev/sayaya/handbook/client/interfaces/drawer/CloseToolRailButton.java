package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.Event;
import org.jboss.elemento.EventType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ToolRail 의 첫 아이템으로 표시되는 ← 아이콘 버튼.
 *
 * <p><b>책임:</b> 현재 열려있는 도구 그룹을 닫고 상위 MenuRail 로 복귀시킨다.</p>
 *
 * <p><b>desktop 동작:</b> MenuRail 은 COLLAPSE, ToolRail 은 HIDE 로 직접 전이한다. 도구 선택
 * 상태(MenuSelected) 는 유지되어 사용자가 이전 선택 컨텍스트로 쉽게 돌아올 수 있다.</p>
 *
 * <p><b>모바일 드릴백:</b> 하단 바가 ToolRail → MenuRail 로 스왑되어야 하므로 `MenuSelected`
 * 를 null 로 초기화해 ToolList 를 비운다. 이후 `ToolRailMode.update` 가 HIDE, `MenuRailMode`
 * 가 EXPAND 로 자연스럽게 수렴한다 (하단 바 레이아웃은 {@code [mobile]} 속성이 담당).</p>
 */
@Singleton
public class CloseToolRailButton extends NavigationRailItemElement {
    private final MenuRailMode menu;
    private final ToolRailMode tools;
    private final MenuSelected menuSelected;
    private final ViewportObserver viewport;
    private final MenuHover hover;

    @Inject CloseToolRailButton(MenuRailMode menu, ToolRailMode tools, MenuSelected menuSelected, ViewportObserver viewport, MenuHover hover) {
        this.menu = menu;
        this.tools = tools;
        this.menuSelected = menuSelected;
        this.viewport = viewport;
        this.hover = hover;
        this.element().id = "close-tool-rail";
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-left"))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-left"));
        element().style.marginTop = CSSProperties.MarginTopUnionType.of("auto");
        on(EventType.click, this::closeToolRail);
    }
    private void closeToolRail(Event evt) {
        evt.preventDefault();
        hover.next(null); // 호버 상태 클리어
        if (viewport.isMobileNow()) {
            menuSelected.next(null);
        } else {
            menu.next(MenuRailState.COLLAPSE);
            tools.next(ToolRailState.HIDE);
        }
    }
}
