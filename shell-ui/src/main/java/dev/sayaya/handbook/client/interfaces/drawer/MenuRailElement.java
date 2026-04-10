package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.domain.Menu;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.jboss.elemento.Elements.div;

/**
 * 메뉴 레일 네비게이션 컨테이너.
 *
 * <p><b>책임:</b> MenuList의 메뉴 목록을 정렬하여 렌더링하고,
 * MenuRailMode의 상태(EXPAND/COLLAPSE/HIDE/BOTTOM_NAV)에 따라 레이아웃을 전환한다.
 * 모바일(BOTTOM_NAV)에서는 하단 네비게이션 바로 표시된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuList} — 메뉴 목록 구독</li>
 *   <li>{@link MenuRailMode} — 레일 상태 구독</li>
 *   <li>{@link MenuRailItemFactory} — 메뉴 아이템 생성</li>
 * </ul></p>
 */
@Singleton
public class MenuRailElement implements NavigationRailElement<MenuRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail");
    private final MenuRailItemFactory factory;
    private final List<MenuRailItemElement> children = new LinkedList<>();
    @Inject MenuRailElement(MenuList list, MenuRailMode mode, MenuRailItemFactory factory) {
        this.factory = factory;
        list.distinctUntilChanged().subscribe(this::update);
        mode.distinctUntilChanged().subscribe(this::mode);
    }
    private static final Comparator<Menu> MENU_COMPARATOR = nullsLast(comparing((Menu i) -> TRUE.equals(i.bottom())).thenComparing(Menu::order));
    private void update(List<Menu> menu) {
        clear();
        if(menu == null) return;
        AtomicBoolean bottom = new AtomicBoolean(false);
        menu.stream().sorted(MENU_COMPARATOR).map(item -> createItem(item, bottom)).forEach(this::add);
    }
    private MenuRailItemElement createItem(Menu menu, AtomicBoolean bottom) {
        var child = factory.item(menu);
        if(TRUE.equals(menu.bottom()) && !bottom.getAndSet(true)) child.element().style.marginTop = CSSProperties.MarginTopUnionType.of("auto");
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
            case BOTTOM_NAV -> bottomNav();
        }
    }

    private void bottomNav() {
        element().removeAttribute("expand");
        element().removeAttribute("collapse");
        element().removeAttribute("hide");
        element().setAttribute("bottom-nav", true);
    }
}
