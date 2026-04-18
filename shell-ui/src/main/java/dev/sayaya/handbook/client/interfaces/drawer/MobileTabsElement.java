package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.ResponsiveOverflow;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.TabsElementBuilder;
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
    /**
     * sayaya-ui {@link TabsElementBuilder} primary 빌더. 기존 직접 {@code htmlContainer("md-tabs")}
     * 호출을 대체해 다른 sayaya-ui 컴포넌트와 빌더 패턴 일관성을 맞춘다. {@code element()} 는
     * 내부적으로 md-tabs Material Web 커스텀 엘리먼트이므로 {@code scrollable/active} 어트리뷰트
     * 직접 제어와 자식 {@code md-primary-tab} append 는 그대로 호환된다.
     */
    private final TabsElementBuilder.TabsPrimaryElementBuilder tabs = TabsElementBuilder.tabs().primary();

    private final NavEntryFactory entries;
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
    MobileTabsElement(NavEntryFactory entries, OverflowMenuController overflow) {
        this.entries = entries;
        this.overflow = overflow;
        tabs.element().classList.add("menu-tabs-bar");
        _this.add(tabs.element()).add(overflow);
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
        // sayaya-ui tab() 이 호출 시점에 md-tabs 에 자동 attach 하므로 별도 appendChild 불필요.
        // overflow 분할 등에서 detach 후 복귀할 때만 직접 appendChild 사용.
        if (topMenus != null) for (Menu m : topMenus) topEntries.add(createEntry(m, false));
        if (bottomMenus != null) for (Menu m : bottomMenus) bottomEntries.add(createEntry(m, true));
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
            HTMLElement tab = entries.populateToolTab(tabs.tab(), t);
            toolEntries.add(new ToolEntry(t, tab));
        }
        overflow.setHidden(true); // tool 모드에선 overflow 버튼 숨김 — 도구는 스크롤만.
        tabs.element().removeAttribute("scrollable");
        elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayoutToolMode());
    }

    /**
     * 선택된 메뉴에 따라 활성 탭을 동기화한다. md-tabs 의 {@code activeTabIndex} property 로
     * 위임하여 click 경로와 URL-resolver 경로를 md-tabs 내부 상태 머신에 수렴시킨다 —
     * 직접 {@code setAttribute("active")} 는 md-tabs 내부 상태와 race 해 indicator 이동
     * 애니메이션을 끊는 회귀가 있어 사용하지 않는다. overflow 팝업의 md-menu-item 은
     * md-tabs 밖이라 {@code selected} 속성을 별도로 수동 관리.
     */
    public void setActive(Menu menu) {
        if (mode != Mode.MENU) return;
        int idx = findVisibleTabIndex(menu);
        if (idx >= 0) tabs.activeTabIndex(idx);
        for (TabEntry e : bottomEntries) {
            if (e.menuItem() == null) continue;
            boolean shouldSelected = e.menu().equals(menu);
            boolean isSelected = e.menuItem().hasAttribute("selected");
            if (shouldSelected && !isSelected) e.menuItem().setAttribute("selected", true);
            else if (!shouldSelected && isSelected) e.menuItem().removeAttribute("selected");
        }
    }

    public void setActiveTool(Tool tool) {
        if (mode != Mode.TOOL) return;
        int idx = -1;
        for (int i = 0; i < toolEntries.size(); i++) {
            if (toolEntries.get(i).tool().equals(tool)) { idx = i; break; }
        }
        if (idx >= 0) tabs.activeTabIndex(idx);
    }

    /**
     * 현재 md-tabs 의 children 중 {@code menu} 와 연결된 탭의 시각적 index.
     * overflow 로 빠진 탭은 md-tabs 의 children 에 없어 자동 제외된다.
     */
    private int findVisibleTabIndex(Menu menu) {
        elemental2.dom.Element cur = tabs.element().firstElementChild;
        int i = 0;
        while (cur != null) {
            for (TabEntry e : topEntries) {
                if (e.tab() == cur && e.menu().equals(menu)) return i;
            }
            for (TabEntry e : bottomEntries) {
                if (e.tab() == cur && e.menu().equals(menu)) return i;
            }
            cur = cur.nextElementSibling;
            i++;
        }
        return -1;
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
        double topWidth = sumWidth(topEntries.stream().map(TabEntry::tab).toList());
        double bottomWidth = sumWidth(bottomEntries.stream().map(TabEntry::tab).toList());
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
        double total = sumWidth(toolEntries.stream().map(ToolEntry::tab).toList());
        if (total > container + 1) tabs.element().setAttribute("scrollable", true);
        else tabs.element().removeAttribute("scrollable");
    }

    private TabEntry createEntry(Menu menu, boolean isBottom) {
        // tabs.tab() 은 new md-primary-tab 을 생성하면서 parent md-tabs 에 자동 attach.
        // entries.populateMenuTab 은 그 builder 에 아이콘/라벨/click/i18n 을 주입.
        HTMLElement tab = entries.populateMenuTab(tabs.tab(), menu);
        HTMLElement menuItem = isBottom ? entries.renderMenuItem(menu, overflow::close) : null;
        return new TabEntry(menu, tab, menuItem);
    }

    private void clearAll() {
        for (TabEntry e : topEntries) detach(e.tab());
        for (TabEntry e : bottomEntries) {
            detach(e.tab());
            if (e.menuItem() != null) detach(e.menuItem());
        }
        topEntries.clear();
        bottomEntries.clear();
    }

    private void clearTools() {
        for (ToolEntry e : toolEntries) detach(e.tab());
        toolEntries.clear();
    }

    private void leaveToolMode() {
        clearTools();
        detachBackButton();
        pendingOnBack = null;
    }

    private void ensureBackButton() {
        if (backButton == null) {
            // sayaya-ui IconButtonElementBuilder 로 md-icon-button 감싼다 — htmlContainer 직접
            // 생성 대신 빌더 패턴 일관성 + HasAriaLabel.ariaLabel() + ElementEventMethods.on() 체이닝.
            backButton = new IconButtonElementBuilder.PlainIconButtonElementBuilder()
                    .css("menu-tabs-back-btn")
                    .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-arrow-left"))
                    .ariaLabel("Back")
                    .on(EventType.click, evt -> {
                        if (pendingOnBack != null) pendingOnBack.run();
                    })
                    .element();
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
                detach(e.tab());
                if (e.menuItem() != null) overflow.addItem(e.menuItem());
            }
        } else {
            for (TabEntry e : bottomEntries) {
                if (e.menuItem() != null) overflow.removeItem(e.menuItem());
                tabs.element().appendChild(e.tab());
            }
        }
    }

    @Override
    public HTMLElement element() { return _this.element(); }

    private enum Mode { MENU, TOOL }

    /** menu 모드 탭 엔트리 — md-primary-tab (tab) 과 overflow 팝업 md-menu-item (menuItem) 의 쌍. */
    private record TabEntry(Menu menu, HTMLElement tab, HTMLElement menuItem) {}

    /** tool 모드 탭 엔트리 — overflow 분할이 없으므로 menuItem 불필요. */
    private record ToolEntry(Tool tool, HTMLElement tab) {}
}
