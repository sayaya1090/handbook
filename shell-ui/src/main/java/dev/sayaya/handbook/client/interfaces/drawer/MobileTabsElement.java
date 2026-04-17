package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.ResponsiveOverflow;
import dev.sayaya.handbook.domain.Menu;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
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
 * <p><b>책임 (View 전용):</b> DOM 구조(md-tabs + overflow slot), 탭 엔트리 추가/제거, 레이아웃
 * 재계산({@link ResponsiveOverflow}), 선택 상태 동기화, 모바일/데스크톱 가시성. 구독·이벤트
 * 오케스트레이션은 {@link MobileTabsPresenter} 에 위임된다 (SRP).</p>
 *
 * <p><b>외부 API:</b> {@link #setEntries(List, List)} / {@link #setActive(Menu)} /
 * {@link #setMobile(boolean)} / {@link #recomputeLayout()}. Presenter 가 MenuList/MenuSelected
 * /ViewportObserver/window resize 를 감지해 이들을 호출한다.</p>
 *
 * <p><b>3단계 반응형 폴백</b> ({@link ResponsiveOverflow}):
 * <ol>
 *   <li><b>평면</b> — 전체 viewport 내 포함: 모두 md-tabs, overflow 버튼 숨김</li>
 *   <li><b>overflow</b> — 상단정렬 + 예약폭 포함: 하단정렬 {@link OverflowMenuController} 팝업으로 수렴</li>
 *   <li><b>스크롤</b> — 상단정렬조차 넘침: {@code md-tabs[scrollable]} 가로 스크롤 + sticky overflow</li>
 * </ol></p>
 */
@Singleton
public class MobileTabsElement implements IsElement<HTMLElement> {

    /** overflow 버튼 예약 폭(px). {@link ResponsiveOverflow#compute} 에 전달. */
    private static final int RESERVE_PX = 48;

    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("menu-tabs");
    private final HTMLContainerBuilder<HTMLElement> tabs = htmlContainer("md-tabs", HTMLElement.class).css("menu-tabs-bar");

    private final MenuTabRenderer renderer;
    private final OverflowMenuController overflow;
    private final List<TabEntry> topEntries = new LinkedList<>();
    private final List<TabEntry> bottomEntries = new LinkedList<>();
    private boolean partitioned = false;

    @Inject
    MobileTabsElement(MenuTabRenderer renderer, OverflowMenuController overflow) {
        this.renderer = renderer;
        this.overflow = overflow;
        _this.add(tabs).add(overflow);
        // 기본 hide — Presenter 가 viewport 에 따라 토글 (flash 방지를 위해 기본은 숨김).
        element().setAttribute("hide", true);
    }

    /**
     * 정렬된 상단/하단 메뉴 리스트를 한 번에 전달받아 DOM 을 재구성한다. 호출자(Presenter)
     * 가 appBarSlot 필터링·정렬 책임을 가진다.
     */
    public void setEntries(List<Menu> topMenus, List<Menu> bottomMenus) {
        clearAll();
        if (topMenus != null) for (Menu m : topMenus) {
            TabEntry e = createEntry(m, false);
            topEntries.add(e);
            tabs.element().appendChild(e.tab);
        }
        if (bottomMenus != null) for (Menu m : bottomMenus) {
            TabEntry e = createEntry(m, true);
            bottomEntries.add(e);
            tabs.element().appendChild(e.tab); // 초기 평면, recomputeLayout 에서 필요 시 overflow 로 이동
        }
        partitioned = false;
        // 레이아웃 측정은 next frame — 웹컴포넌트 shadow DOM stamp 후여야 폭 정확.
        elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
    }

    /** 선택 메뉴를 탭/메뉴아이템의 active/selected 속성과 동기화. */
    public void setActive(Menu menu) {
        for (TabEntry e : topEntries) applyActive(e, menu);
        for (TabEntry e : bottomEntries) applyActive(e, menu);
    }

    /** 뷰포트 모바일 여부에 따라 [hide] 속성 토글 + 레이아웃 재계산 예약. */
    public void setMobile(boolean mobile) {
        if (mobile) {
            element().removeAttribute("hide");
            elemental2.dom.DomGlobal.requestAnimationFrame(ts -> recomputeLayout());
        } else {
            element().setAttribute("hide", true);
        }
    }

    /** 컨테이너 폭 vs 탭 합산 폭을 기반으로 3단계 폴백 상태 갱신 (Presenter 의 resize 훅에서도 호출). */
    public void recomputeLayout() {
        if (element().hasAttribute("hide")) return;
        double container = element().clientWidth;
        if (container <= 0) return;
        double topWidth = sumWidth(topEntries);
        double bottomWidth = sumWidth(bottomEntries);
        ResponsiveOverflow.Result r = ResponsiveOverflow.compute(container, topWidth, bottomWidth, RESERVE_PX);
        setPartitioned(r.showOverflow);
        if (r.scrollable) tabs.element().setAttribute("scrollable", true);
        else tabs.element().removeAttribute("scrollable");
        overflow.setHidden(!r.showOverflow);
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

    private static void detach(HTMLElement el) {
        if (el != null && el.parentNode != null) el.parentNode.removeChild(el);
    }

    private double sumWidth(List<TabEntry> entries) {
        double total = 0;
        for (TabEntry e : entries) {
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
