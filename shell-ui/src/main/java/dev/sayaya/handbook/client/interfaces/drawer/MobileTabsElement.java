package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.ResponsiveOverflow;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlContainer;
import static org.jboss.elemento.Elements.span;

/**
 * 모바일 뷰포트 전용 상단 Scrollable Tabs 네비게이션.
 *
 * <p><b>책임:</b> 데스크톱 {@link MenuRailElement} 를 대체해 모바일에서 상단 고정
 * {@code md-tabs} 로 메뉴 엔트리를 노출한다. 배치 규칙은:
 * <ol>
 *   <li>상단정렬 공급자({@code bottom=false}) — {@code order} 오름차순 leading 배치</li>
 *   <li>하단정렬 공급자({@code bottom=true}) — {@code order} 내림차순 trailing 배치</li>
 * </ol>
 * 데스크톱에서 세로 축 "아래일수록 중요" semantic 을 가로 축 "왼쪽일수록 중요" 매핑으로
 * 보존하기 위함. `login (order=Z, bottom=true)` 이 하단정렬 그룹의 leading 에 와서 세션
 * 토글 접근성 유지. 상세 정책은 {@code docs/contracts/menus.md#소비자-렌더-정책}.</p>
 *
 * <p><b>3단계 반응형 폴백</b> ({@link ResponsiveOverflow}):
 * <ol>
 *   <li><b>평면</b> — 전체가 viewport 에 들어감: 모두 md-tabs 에 표시, overflow 버튼 숨김</li>
 *   <li><b>overflow</b> — 상단정렬 + 예약폭 은 들어감: 하단정렬은 md-menu 팝업으로 이동, trailing 에 md-icon-button(…) 표시</li>
 *   <li><b>스크롤</b> — 상단정렬조차 넘침: 탭은 가로 스크롤, overflow 버튼은 sticky trailing 유지</li>
 * </ol>
 * 재계산 트리거: (1) {@link MenuList} 변경, (2) window resize, (3) {@link LabelProvider} 변경.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuList} — 메뉴 목록 구독</li>
 *   <li>{@link MenuSelected} — 양방향 active 동기화 (click → next, 구독 → active 속성)</li>
 *   <li>{@link LabelProvider} — i18n 라벨</li>
 *   <li>{@link ViewportObserver} — 모바일 여부 (데스크톱에서 [hide])</li>
 * </ul></p>
 *
 * <p><b>주의:</b> {@code md-menu} 웹컴포넌트는 shadow DOM 내부에 팝업을 렌더하므로 외부
 * CSS 가 내부 레이아웃을 간섭하지 않는다. anchor 는 {@code md-icon-button} 의 id 로 연결.</p>
 */
@Singleton
public class MobileTabsElement implements IsElement<HTMLElement> {

    /** overflow 버튼 예약 폭(px). {@code ResponsiveOverflow.compute} 에 전달. */
    private static final int RESERVE_PX = 48;
    private static final String OVERFLOW_BTN_ID = "menu-tabs-overflow-btn";

    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("menu-tabs");
    private final HTMLContainerBuilder<HTMLElement> tabs = htmlContainer("md-tabs", HTMLElement.class).css("menu-tabs-bar");
    private final HTMLContainerBuilder<HTMLElement> overflowBtn = htmlContainer("md-icon-button", HTMLElement.class)
            .css("menu-tabs-overflow-btn");
    private final HTMLContainerBuilder<HTMLElement> overflowMenu = htmlContainer("md-menu", HTMLElement.class)
            .css("menu-tabs-overflow-menu");

    private final MenuSelected selected;
    private final LabelProvider labelProvider;
    private final List<TabEntry> topEntries = new LinkedList<>();
    private final List<TabEntry> bottomEntries = new LinkedList<>();
    private boolean partitioned = false;

    @Inject
    MobileTabsElement(MenuList list, MenuSelected selected, LabelProvider labelProvider, ViewportObserver viewport) {
        this.selected = selected;
        this.labelProvider = labelProvider;

        overflowBtn.element().id = OVERFLOW_BTN_ID;
        overflowBtn.element().setAttribute("aria-label", "More");
        overflowBtn.add(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-ellipsis"));
        overflowBtn.on(EventType.click, evt -> toggleMenu());
        overflowMenu.attr("anchor", OVERFLOW_BTN_ID).attr("positioning", "absolute");
        // md-menu 는 close-menu 이벤트로 팝업 닫힘을 알린다 (md-menu-item 선택 or 외부 클릭).
        overflowMenu.element().addEventListener("close-menu", e -> overflowMenu.element().removeAttribute("open"));

        _this.add(tabs).add(overflowBtn).add(overflowMenu);
        hideOverflowBtn(true);

        element().setAttribute("hide", true);
        if (viewport.isMobileNow()) element().removeAttribute("hide");
        viewport.isMobile().subscribe(this::setMobile);
        list.distinctUntilChanged().subscribe(this::update);
        selected.subscribe(this::onSelected);
        DomGlobal.window.addEventListener("resize", e -> recomputeLayout());
    }

    private static final Comparator<Menu> TOP_COMPARATOR = comparing(Menu::order);
    private static final Comparator<Menu> BOTTOM_COMPARATOR = comparing(Menu::order, Comparator.reverseOrder());

    private void update(List<Menu> menus) {
        clearAll();
        if (menus == null) return;
        List<Menu> top = new ArrayList<>();
        List<Menu> bottom = new ArrayList<>();
        for (Menu m : menus) {
            if (TRUE.equals(m.bottom())) bottom.add(m);
            else top.add(m);
        }
        top.sort(TOP_COMPARATOR);
        bottom.sort(BOTTOM_COMPARATOR);
        for (Menu m : top) {
            TabEntry e = createEntry(m, false);
            topEntries.add(e);
            tabs.element().appendChild(e.tab);
        }
        for (Menu m : bottom) {
            TabEntry e = createEntry(m, true);
            bottomEntries.add(e);
            tabs.element().appendChild(e.tab); // 초기는 평면, recomputeLayout 에서 필요 시 menu 로 이동
        }
        partitioned = false;
        // 레이아웃 측정은 next frame 에서 — 웹컴포넌트 shadow DOM 이 stamp 된 후여야 폭이 정확.
        DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
    }

    private TabEntry createEntry(Menu menu, boolean isBottom) {
        HTMLContainerBuilder<HTMLElement> tab = htmlContainer("md-primary-tab", HTMLElement.class).css("menu-tab");
        HTMLElement iconOutline = IconElementBuilder.icon()
                .css("fa-sharp", "fa-light", menu.icon(), "icon-outline").element();
        iconOutline.setAttribute("slot", "icon");
        HTMLElement iconFilled = IconElementBuilder.icon()
                .css("fa-sharp", "fa-solid", menu.icon(), "icon-filled").element();
        iconFilled.setAttribute("slot", "active-icon");
        tab.add(iconOutline).add(iconFilled);
        HTMLElement label = span().css("menu-tab-label").element();
        tab.add(label);
        if (menu.title() != null) tab.element().dataset.set("menuTitle", menu.title());
        tab.on(EventType.click, evt -> selected.next(menu));

        HTMLElement menuItem = null;
        if (isBottom) {
            HTMLContainerBuilder<HTMLElement> mi = htmlContainer("md-menu-item", HTMLElement.class).css("menu-tab-menu-item");
            HTMLElement miIcon = IconElementBuilder.icon()
                    .css("fa-sharp", "fa-light", menu.icon(), "icon-outline").element();
            miIcon.setAttribute("slot", "start");
            mi.add(miIcon);
            HTMLElement miHeadline = div().element();
            miHeadline.setAttribute("slot", "headline");
            mi.add(miHeadline);
            if (menu.title() != null) mi.element().dataset.set("menuTitle", menu.title());
            mi.on(EventType.click, evt -> {
                selected.next(menu);
                overflowMenu.element().removeAttribute("open");
            });
            menuItem = mi.element();
            // menu-item headline 도 i18n 갱신 대상
            final HTMLElement miHeadlineRef = miHeadline;
            labelProvider.subscribe(labels -> {
                String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
                miHeadlineRef.textContent = title;
            });
        }

        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            label.textContent = title;
        });

        return new TabEntry(menu, tab.element(), menuItem);
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

    private static void detach(HTMLElement el) {
        if (el != null && el.parentNode != null) el.parentNode.removeChild(el);
    }

    private void recomputeLayout() {
        if (element().hasAttribute("hide")) return;
        double container = element().clientWidth;
        if (container <= 0) return; // 아직 레이아웃 전
        double topWidth = sumWidth(topEntries);
        double bottomWidth = sumWidth(bottomEntries);
        ResponsiveOverflow.Result r = ResponsiveOverflow.compute(container, topWidth, bottomWidth, RESERVE_PX);
        setPartitioned(r.showOverflow);
        if (r.scrollable) tabs.element().setAttribute("scrollable", true);
        else tabs.element().removeAttribute("scrollable");
        hideOverflowBtn(!r.showOverflow);
    }

    private double sumWidth(List<TabEntry> entries) {
        double total = 0;
        for (TabEntry e : entries) {
            // 분리 모드에서는 menuItem 쪽이라 tab 의 offsetWidth 가 0 일 수 있음 — 재측정 위해 tab 은
            // 분리 전 마지막 폭을 신뢰하지 말고, offsetWidth 가 0 이면 96(min-width) 로 가정.
            double w = e.tab.offsetWidth;
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
                if (e.menuItem != null) overflowMenu.element().appendChild(e.menuItem);
            }
        } else {
            for (TabEntry e : bottomEntries) {
                if (e.menuItem != null) detach(e.menuItem);
                tabs.element().appendChild(e.tab);
            }
        }
    }

    private void toggleMenu() {
        if (overflowMenu.element().hasAttribute("open")) overflowMenu.element().removeAttribute("open");
        else overflowMenu.element().setAttribute("open", true);
    }

    private void hideOverflowBtn(boolean hide) {
        if (hide) overflowBtn.element().setAttribute("hidden", true);
        else overflowBtn.element().removeAttribute("hidden");
    }

    private void onSelected(Menu menu) {
        for (TabEntry e : topEntries) applyActive(e, menu);
        for (TabEntry e : bottomEntries) applyActive(e, menu);
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

    private void setMobile(boolean mobile) {
        if (mobile) {
            element().removeAttribute("hide");
            DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
        } else {
            element().setAttribute("hide", true);
        }
    }

    @Override
    public HTMLElement element() { return _this.element(); }

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
}
