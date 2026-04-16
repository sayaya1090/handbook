package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.jboss.elemento.Elements.div;

/**
 * 메뉴 레일 네비게이션 컨테이너.
 *
 * <p><b>책임:</b> MenuList의 메뉴 목록을 정렬하여 렌더링하고,
 * MenuRailMode의 상태(EXPAND/COLLAPSE/HIDE)에 따라 가시성을 전환한다.
 * 모바일 여부는 직교하는 {@code [mobile]} 속성으로 관리되며, 해당 속성이 걸리면 CSS 가
 * rail 을 하단 고정 바 레이아웃으로 전환한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuList} — 메뉴 목록 구독</li>
 *   <li>{@link MenuRailMode} — 레일 가시성 상태 구독</li>
 *   <li>{@link ViewportObserver} — 모바일/데스크톱 뷰포트 구독</li>
 *   <li>{@link MenuRailItemFactory} — 메뉴 아이템 생성</li>
 * </ul></p>
 */
@Singleton
public class MenuRailElement implements NavigationRailElement<MenuRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail");
    private final MenuRailItemFactory factory;
    private final List<MenuRailItemElement> children = new LinkedList<>();
    @Inject MenuRailElement(MenuList list, MenuRailMode mode, MenuRailItemFactory factory, ViewportObserver viewport) {
        this.factory = factory;
        // 초기 가시성은 HIDE. [mobile] 을 mode 구독보다 먼저 설정하지 않으면 BehaviorSubject
        // 의 즉시 emit 으로 expand() 가 호출되어 한 프레임 동안 desktop [expand] 레이아웃
        // (좌측 컬럼) 이 노출된 뒤 [mobile] 이 붙어 하단 바로 점프하는 flash 가 생긴다.
        element().setAttribute("hide", true);
        if (viewport.isMobileNow()) element().setAttribute("mobile", true);
        list.distinctUntilChanged().subscribe(this::update);
        mode.distinctUntilChanged().subscribe(this::mode);
        viewport.isMobile().subscribe(this::setMobile);
    }
    private static final Comparator<Menu> MENU_COMPARATOR = nullsLast(comparing((Menu i) -> TRUE.equals(i.bottom())).thenComparing(Menu::order));
    private void update(List<Menu> menu) {
        clear();
        if(menu == null) return;
        menu.stream().sorted(MENU_COMPARATOR).map(this::createItem).forEach(this::add);
    }
    private MenuRailItemElement createItem(Menu menu) {
        var child = factory.item(menu);
        if(TRUE.equals(menu.bottom())) child.element().classList.add("bottom-menu");
        children.add(child);
        return child;
    }
    private void clear() {
        for(var child : children) child.element().remove();
        children.clear();
    }
    private void mode(MenuRailState state) {
        switch (state) {
            case EXPAND -> expand();
            case COLLAPSE -> collapse();
            case HIDE -> hide();
        }
    }
    private void setMobile(boolean mobile) {
        if (mobile) element().setAttribute("mobile", true);
        else element().removeAttribute("mobile");
    }
}
