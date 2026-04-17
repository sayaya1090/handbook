package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;

/**
 * {@link MobileTabsElement} 의 구독·이벤트 오케스트레이션.
 *
 * <p><b>책임:</b> {@link MenuList}/{@link MenuSelected}/{@link ViewportObserver}/window resize
 * 네 가지 외부 소스를 구독하여 View 의 해당 API ({@link MobileTabsElement#setEntries},
 * {@link MobileTabsElement#setActive}, {@link MobileTabsElement#setMobile},
 * {@link MobileTabsElement#recomputeLayout}) 로 전달한다. 또한 MenuList 원본에서
 * {@code appBarSlot != null} 항목을 필터링하고 상단정렬(bottom=false, order asc) +
 * 하단정렬(bottom=true, order desc) 로 정렬해 View 에 리스트 쌍을 넘긴다.</p>
 *
 * <p><b>SOLID 반영:</b>
 * <ul>
 *   <li>S: 순수한 presentation 바인딩. DOM 조립 · 레이아웃 계산은 View 에 위임.</li>
 *   <li>D: View 의 추상 API 4 개에만 의존. 구체 DOM 지식 없음.</li>
 *   <li>O: 새 외부 소스(예: 테마 전환 구독) 추가 시 Presenter 생성자에 주입만 더하면 됨 —
 *       View 수정 불필요.</li>
 * </ul></p>
 *
 * <p><b>초기화:</b> {@link javax.inject.Singleton} + {@link Inject} — Dagger 그래프가 Presenter
 * 를 참조하는 순간(예: ShellInitializer 의 생성자 주입) 구독이 시작된다. 별도 init() 호출
 * 불필요.</p>
 */
@Singleton
public class MobileTabsPresenter {

    private static final Comparator<Menu> TOP_COMPARATOR = comparing(Menu::order);
    private static final Comparator<Menu> BOTTOM_COMPARATOR = comparing(Menu::order, Comparator.reverseOrder());

    @Inject
    MobileTabsPresenter(MenuList list, MenuSelected selected, ViewportObserver viewport, MobileTabsElement view) {
        // 초기 viewport 상태를 먼저 반영 — MenuList/MenuSelected 의 즉시 emit 보다 앞서 뷰를
        // 모바일/데스크톱 기준 hide 상태로 세팅하여 flash 방지.
        view.setMobile(viewport.isMobileNow());

        list.distinctUntilChanged().subscribe(menus -> view.setEntries(filterAndSortTop(menus), filterAndSortBottom(menus)));
        selected.subscribe(view::setActive);
        viewport.isMobile().subscribe(view::setMobile);
        DomGlobal.window.addEventListener("resize", e -> view.recomputeLayout());
    }

    private static List<Menu> filterAndSortTop(List<Menu> menus) {
        if (menus == null) return List.of();
        List<Menu> out = new ArrayList<>();
        for (Menu m : menus) {
            if (m.appBarSlot() != null) continue;
            if (!TRUE.equals(m.bottom())) out.add(m);
        }
        out.sort(TOP_COMPARATOR);
        return out;
    }

    private static List<Menu> filterAndSortBottom(List<Menu> menus) {
        if (menus == null) return List.of();
        List<Menu> out = new ArrayList<>();
        for (Menu m : menus) {
            if (m.appBarSlot() != null) continue;
            if (TRUE.equals(m.bottom())) out.add(m);
        }
        out.sort(BOTTOM_COMPARATOR);
        return out;
    }
}
