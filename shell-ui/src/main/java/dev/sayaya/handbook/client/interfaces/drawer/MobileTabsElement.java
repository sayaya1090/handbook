package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.ui.elements.IconElementBuilder;
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
 * 보존하기 위함. `login (order=Z, bottom=true)` 이 하단정렬 leading 에 와서 세션 토글
 * 접근성 유지. 상세 정책은 {@code docs/contracts/menus.md#소비자-렌더-정책}.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuList} — 메뉴 목록 구독</li>
 *   <li>{@link MenuSelected} — 양방향 active 동기화 (click → next, 구독 → active 속성)</li>
 *   <li>{@link LabelProvider} — i18n 라벨</li>
 *   <li>{@link ViewportObserver} — 모바일 여부</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 이 컴포넌트는 모바일일 때만 {@code display:block}, 데스크톱에서는
 * {@code [hide]} 속성이 걸려 숨겨진다 ({@code .menu-tabs[hide]} CSS 규칙). MenuRail 은
 * 반대로 {@code .menu-rail[mobile]} 에서 숨겨져 두 레이아웃이 상호 배타 동작한다.</p>
 *
 * <p><b>TODO:</b> overflow 버튼({@code md-icon-button}) + {@code md-menu} 팝업 기반 3단계
 * 반응형 폴백은 후속 커밋. 현재는 {@code overflow-x:auto} 자연 스크롤만 제공.</p>
 */
@Singleton
public class MobileTabsElement implements IsElement<HTMLElement> {

    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("menu-tabs");
    private final HTMLContainerBuilder<HTMLElement> tabs = htmlContainer("md-tabs", HTMLElement.class).css("menu-tabs-bar");
    private final MenuSelected selected;
    private final LabelProvider labelProvider;
    private final List<TabEntry> entries = new LinkedList<>();

    @Inject
    MobileTabsElement(MenuList list, MenuSelected selected, LabelProvider labelProvider, ViewportObserver viewport) {
        this.selected = selected;
        this.labelProvider = labelProvider;
        _this.add(tabs);
        // 초기 상태는 hidden — 뷰포트 구독 전에 깜빡임 방지. mobileNow 면 즉시 해제.
        element().setAttribute("hide", true);
        if (viewport.isMobileNow()) element().removeAttribute("hide");
        viewport.isMobile().subscribe(this::setMobile);
        list.distinctUntilChanged().subscribe(this::update);
        selected.subscribe(this::onSelected);
    }

    private static final Comparator<Menu> TOP_COMPARATOR = comparing(Menu::order);
    private static final Comparator<Menu> BOTTOM_COMPARATOR = comparing(Menu::order, Comparator.reverseOrder());

    private void update(List<Menu> menus) {
        clear();
        if (menus == null) return;
        List<Menu> top = new ArrayList<>();
        List<Menu> bottom = new ArrayList<>();
        for (Menu m : menus) {
            if (TRUE.equals(m.bottom())) bottom.add(m);
            else top.add(m);
        }
        top.sort(TOP_COMPARATOR);
        bottom.sort(BOTTOM_COMPARATOR);
        for (Menu m : top) addTab(m);
        for (Menu m : bottom) addTab(m);
    }

    private void addTab(Menu menu) {
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
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            label.textContent = title;
        });
        if (menu.title() != null) tab.element().dataset.set("menuTitle", menu.title());
        tab.on(EventType.click, evt -> selected.next(menu));
        tabs.add(tab);
        entries.add(new TabEntry(menu, tab.element()));
    }

    private void clear() {
        for (TabEntry e : entries) e.element.remove();
        entries.clear();
    }

    private void onSelected(Menu menu) {
        for (TabEntry e : entries) {
            boolean active = e.menu.equals(menu);
            if (active) e.element.setAttribute("active", true);
            else e.element.removeAttribute("active");
        }
    }

    private void setMobile(boolean mobile) {
        if (mobile) element().removeAttribute("hide");
        else element().setAttribute("hide", true);
    }

    @Override
    public HTMLElement element() { return _this.element(); }

    private static class TabEntry {
        final Menu menu;
        final HTMLElement element;
        TabEntry(Menu menu, HTMLElement element) {
            this.menu = menu;
            this.element = element;
        }
    }
}
