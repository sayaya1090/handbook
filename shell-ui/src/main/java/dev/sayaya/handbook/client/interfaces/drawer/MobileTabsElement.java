package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.ResponsiveOverflow;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlContainer;

/**
 * 모바일 뷰포트 전용 상단 Scrollable Tabs — <b>뷰 컴포넌트</b>.
 *
 * <p><b>책임 (View 전용):</b> DOM 구조(md-tabs + overflow slot + back button), 탭 엔트리
 * 추가/제거, 레이아웃 재계산({@link ResponsiveOverflow}), 선택 상태 동기화, 뷰포트/모드 전환.
 * 구독·이벤트 오케스트레이션은 {@link MobileTabsPresenter} 에 위임된다 (SRP).</p>
 *
 * <p><b>두 모드:</b>
 * <ul>
 *   <li><b>Menu mode</b> — {@link #setEntries(List, List)} 로 MenuList 상단/하단 정렬본
 *       을 받아 표시 (모바일 기본 네비).</li>
 *   <li><b>Tool mode</b> — {@link #setToolEntries(List, Runnable)} 로 현재 메뉴의 도구
 *       목록을 받아 표시. 드릴인 패턴 — leading 에 back 버튼(← 아이콘), 클릭 시 전달된
 *       {@code onBack} 으로 메뉴 모드 복귀 (일반적으로 MenuSelected.next(null)).</li>
 * </ul>
 * 이전까지 {@code .rail.tool-rail[mobile][expand]} 가 담당하던 하단 바 드릴인을 상단 Tabs
 * 로 흡수 — Agent input dock 과의 z-index 겹침 문제 해소.</p>
 *
 * <p><b>3단계 반응형 폴백</b> ({@link ResponsiveOverflow}): 평면 / overflow 수렴 / 스크롤.</p>
 */
@Singleton
public class MobileTabsElement implements IsElement<HTMLElement> {

    private static final int RESERVE_PX = 48;

    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("menu-tabs");
    private final HTMLContainerBuilder<HTMLElement> tabs = htmlContainer("md-tabs", HTMLElement.class).css("menu-tabs-bar");

    private final MenuTabRenderer renderer;
    private final OverflowMenuController overflow;
    /** menu mode 상단정렬 탭. */
    private final List<TabEntry> topEntries = new LinkedList<>();
    /** menu mode 하단정렬 탭 (overflow 수렴 가능). */
    private final List<TabEntry> bottomEntries = new LinkedList<>();
    /** tool mode 탭 (간단 리스트 — overflow 수렴 X, 스크롤만). */
    private final List<ToolEntry> toolEntries = new LinkedList<>();
    private boolean partitioned = false;
    private Mode mode = Mode.MENU;
    /** tool 모드에서 leading 에 삽입되는 back 버튼. 생성은 lazy. */
    private HTMLElement backButton;
    private Runnable pendingOnBack;

    @Inject
    MobileTabsElement(MenuTabRenderer renderer, OverflowMenuController overflow) {
        this.renderer = renderer;
        this.overflow = overflow;
        _this.add(tabs).add(overflow);
        element().setAttribute("hide", true);
    }

    /**
     * Menu 모드로 전환 + 상단/하단 메뉴 리스트 세팅. Presenter 가 appBarSlot 필터링/정렬
     * 후 호출.
     */
    public void setEntries(List<Menu> topMenus, List<Menu> bottomMenus) {
        leaveToolMode();
        mode = Mode.MENU;
        clearAll();
        if (topMenus != null) for (Menu m : topMenus) {
            TabEntry e = createEntry(m, false);
            topEntries.add(e);
            tabs.element().appendChild(e.tab);
        }
        if (bottomMenus != null) for (Menu m : bottomMenus) {
            TabEntry e = createEntry(m, true);
            bottomEntries.add(e);
            tabs.element().appendChild(e.tab);
        }
        partitioned = false;
        elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
    }

    /**
     * Tool 모드로 전환 — 현재 tabs 을 비우고 도구 목록으로 교체. leading 에 back 버튼 삽입.
     *
     * @param tools  현재 선택 메뉴의 도구 목록 (이미 정렬된 상태로 전달)
     * @param onBack 드릴백 콜백 — 일반적으로 {@code () -> menuSelected.next(null)}
     */
    public void setToolEntries(List<Tool> tools, Runnable onBack) {
        mode = Mode.TOOL;
        clearAll();
        clearTools();
        ensureBackButton();
        pendingOnBack = onBack;
        if (tools != null) for (Tool t : tools) {
            HTMLElement tab = renderer.renderToolTab(t);
            toolEntries.add(new ToolEntry(t, tab));
            tabs.element().appendChild(tab);
        }
        overflow.setHidden(true); // tool 모드에선 overflow 버튼 숨김 — 도구는 스크롤만.
        tabs.element().removeAttribute("scrollable");
        elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayoutToolMode());
    }

    /** 선택된 메뉴/도구에 따라 active 상태 동기화. Presenter 가 mode 에 맞는 값만 넘기도록 관리. */
    public void setActive(Menu menu) {
        if (mode != Mode.MENU) return;
        for (TabEntry e : topEntries) applyActive(e, menu);
        for (TabEntry e : bottomEntries) applyActive(e, menu);
    }

    public void setActiveTool(Tool tool) {
        if (mode != Mode.TOOL) return;
        for (ToolEntry e : toolEntries) {
            boolean active = e.tool.equals(tool);
            if (active) e.tab.setAttribute("active", true);
            else e.tab.removeAttribute("active");
        }
    }

    public void setMobile(boolean mobile) {
        if (mobile) {
            element().removeAttribute("hide");
            elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
        } else {
            element().setAttribute("hide", true);
        }
    }

    public void recomputeLayout() {
        if (element().hasAttribute("hide")) return;
        if (mode == Mode.TOOL) {
            recomputeLayoutToolMode();
            return;
        }
        double container = element().clientWidth;
        if (container <= 0) return;
        double topWidth = sumWidth(topEntries.stream().map(e -> e.tab).toList());
        double bottomWidth = sumWidth(bottomEntries.stream().map(e -> e.tab).toList());
        ResponsiveOverflow.Result r = ResponsiveOverflow.compute(container, topWidth, bottomWidth, RESERVE_PX);
        setPartitioned(r.showOverflow);
        if (r.scrollable) tabs.element().setAttribute("scrollable", true);
        else tabs.element().removeAttribute("scrollable");
        overflow.setHidden(!r.showOverflow);
    }

    /** tool 모드는 overflow 분할 없음 — scrollable 여부만 판정. */
    private void recomputeLayoutToolMode() {
        if (element().hasAttribute("hide")) return;
        double container = element().clientWidth;
        if (container <= 0) return;
        double total = sumWidth(toolEntries.stream().map(e -> e.tab).toList());
        if (total > container + 1) tabs.element().setAttribute("scrollable", true);
        else tabs.element().removeAttribute("scrollable");
    }

    private TabEntry createEntry(Menu menu, boolean isBottom) {
        HTMLElement tab = renderer.renderTab(menu);
        HTMLElement menuItem = isBottom ? renderer.renderMenuItem(menu, overflow::close) : null;
        return new TabEntry(menu, tab, menuItem);
    }

    private void clearAll() {
        for (TabEntry e : topEntries) detach(e.tab);
        for (TabEntry e : bottomEntries) {
            detach(e.tab);
            if (e.menuItem != null) detach(e.menuItem);
        }
        topEntries.clear();
        bottomEntries.clear();
    }

    private void clearTools() {
        for (ToolEntry e : toolEntries) detach(e.tab);
        toolEntries.clear();
    }

    private void leaveToolMode() {
        clearTools();
        detachBackButton();
        pendingOnBack = null;
    }

    private void ensureBackButton() {
        if (backButton == null) {
            HTMLContainerBuilder<HTMLElement> btn = htmlContainer("md-icon-button", HTMLElement.class)
                    .css("menu-tabs-back-btn");
            btn.add(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-arrow-left"));
            btn.element().setAttribute("aria-label", "Back");
            btn.on(EventType.click, evt -> {
                if (pendingOnBack != null) pendingOnBack.run();
            });
            backButton = btn.element();
        }
        if (backButton.parentNode == null) {
            _this.element().insertBefore(backButton, tabs.element());
        }
    }

    private void detachBackButton() {
        if (backButton != null && backButton.parentNode != null) {
            backButton.parentNode.removeChild(backButton);
        }
    }

    private static void detach(HTMLElement el) {
        if (el != null && el.parentNode != null) el.parentNode.removeChild(el);
    }

    private double sumWidth(List<HTMLElement> elems) {
        double total = 0;
        for (HTMLElement el : elems) {
            double w = el.offsetWidth;
            if (w <= 0) w = 96.0;
            total += w;
        }
        return total;
    }

    private void setPartitioned(boolean on) {
        if (partitioned == on) return;
        partitioned = on;
        if (on) {
            for (TabEntry e : bottomEntries) {
                detach(e.tab);
                if (e.menuItem != null) overflow.addItem(e.menuItem);
            }
        } else {
            for (TabEntry e : bottomEntries) {
                if (e.menuItem != null) overflow.removeItem(e.menuItem);
                tabs.element().appendChild(e.tab);
            }
        }
    }

    private void applyActive(TabEntry e, Menu selectedMenu) {
        boolean active = e.menu.equals(selectedMenu);
        if (active) e.tab.setAttribute("active", true);
        else e.tab.removeAttribute("active");
        if (e.menuItem != null) {
            if (active) e.menuItem.setAttribute("selected", true);
            else e.menuItem.removeAttribute("selected");
        }
    }

    @Override
    public HTMLElement element() { return _this.element(); }

    private enum Mode { MENU, TOOL }

    private static class TabEntry {
        final Menu menu;
        final HTMLElement tab;
        final HTMLElement menuItem;
        TabEntry(Menu menu, HTMLElement tab, HTMLElement menuItem) {
            this.menu = menu;
            this.tab = tab;
            this.menuItem = menuItem;
        }
    }

    private static class ToolEntry {
        final Tool tool;
        final HTMLElement tab;
        ToolEntry(Tool tool, HTMLElement tab) {
            this.tool = tool;
            this.tab = tab;
        }
    }
}
