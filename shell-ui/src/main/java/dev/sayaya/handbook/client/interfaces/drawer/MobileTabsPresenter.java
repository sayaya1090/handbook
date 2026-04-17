package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.ToolList;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

/**
 * {@link MobileTabsElement} 의 구독·이벤트 오케스트레이션.
 *
 * <p><b>책임:</b> {@link MenuList}/{@link MenuSelected}/{@link ToolList}/{@link ToolSelected}
 * /{@link ViewportObserver}/window resize 여섯 가지 외부 소스를 구독하여 View 의 해당 API
 * 로 전달한다. 모바일에서 도구 2개 이상 메뉴 선택 시 View 를 <b>tool 모드</b> 로 드릴인시켜
 * 상단 탭을 도구 목록으로 교체하고 leading 에 back 버튼을 노출한다.</p>
 *
 * <p><b>모드 전환 규칙:</b>
 * <ul>
 *   <li>{@code ToolList.size() > 1} → View 를 tool 모드로 전환 (도구 탭 + back).</li>
 *   <li>{@code ToolList.size() <= 1} → menu 모드로 복귀 (상단/하단 정렬 메뉴 탭).</li>
 * </ul>
 * 이전에 하단 바 자리를 차지하던 {@link ToolRailElement} 모바일 드릴인을 상단 MobileTabs
 * 로 이관 — Agent input dock 과의 bottom z-index 겹침 문제 해소.</p>
 */
@Singleton
public class MobileTabsPresenter {

    private static final Comparator<Menu> TOP_COMPARATOR = comparing(Menu::order);
    private static final Comparator<Menu> BOTTOM_COMPARATOR = comparing(Menu::order, Comparator.reverseOrder());
    private static final Comparator<Tool> TOOL_COMPARATOR = nullsLast(comparing(Tool::order));

    private final MobileTabsElement view;
    private final MenuSelected menuSelected;
    /** 마지막 MenuList 값 — tool mode 종료 시 menu mode 복구에 사용. */
    private List<Menu> lastMenus = List.of();
    private boolean inToolMode = false;

    @Inject
    MobileTabsPresenter(MenuList menuList, MenuSelected menuSelected,
                        ToolList toolList, ToolSelected toolSelected,
                        ViewportObserver viewport, MobileTabsElement view) {
        this.view = view;
        this.menuSelected = menuSelected;
        // 초기 viewport 상태를 먼저 반영해 flash 방지.
        view.setMobile(viewport.isMobileNow());

        menuList.distinctUntilChanged().subscribe(this::onMenuList);
        menuSelected.subscribe(view::setActive);
        toolList.distinctUntilChanged().subscribe(this::onToolList);
        toolSelected.subscribe(view::setActiveTool);
        viewport.isMobile().subscribe(view::setMobile);
        DomGlobal.window.addEventListener("resize", e -> view.recomputeLayout());
    }

    private void onMenuList(List<Menu> menus) {
        lastMenus = menus != null ? menus : List.of();
        if (!inToolMode) applyMenuEntries();
    }

    private void onToolList(List<Tool> tools) {
        if (tools != null && tools.size() > 1) {
            inToolMode = true;
            List<Tool> sorted = new ArrayList<>(tools);
            sorted.sort(TOOL_COMPARATOR);
            view.setToolEntries(sorted, () -> menuSelected.next(null));
        } else if (inToolMode) {
            inToolMode = false;
            applyMenuEntries();
        }
    }

    private void applyMenuEntries() {
        view.setEntries(filterAndSort(lastMenus, false), filterAndSort(lastMenus, true));
    }

    private static List<Menu> filterAndSort(List<Menu> menus, boolean bottom) {
        List<Menu> out = new ArrayList<>();
        if (menus != null) for (Menu m : menus) {
            if (m.appBarSlot() != null) continue;
            if (TRUE.equals(m.bottom()) == bottom) out.add(m);
        }
        out.sort(bottom ? BOTTOM_COMPARATOR : TOP_COMPARATOR);
        return out;
    }
}
