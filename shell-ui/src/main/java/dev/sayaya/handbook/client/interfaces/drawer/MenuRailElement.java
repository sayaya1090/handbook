package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.domain.Menu;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
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
 * <p><b>햄버거 토글 (2026-04-18 이관 완료):</b> {@link MenuToggleButton} 은 **MenuRail 이
 * 아니라 DrawerElement 의 첫 자식**으로 mount 된다. 이유: MenuRail 이 HIDE(width:0 +
 * overflow:hidden) 상태여도 drawer 자체가 visible 인 동안에는 햄버거가 보여야 하기 때문.
 * rail 의 자식으로 두면 rail[hide] 에 걸려 함께 잘린다. 모바일([mobile]) 에서는 CSS
 * `.drawer:has(.menu-rail[mobile]) > #menu-toggle-button { display:none }` 로 숨긴다.
 * MenuRail 은 순수 메뉴 아이템 컨테이너 역할만 담당.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuRailItemFactory} — 메뉴 아이템 생성</li>
 * </ul></p>
 */
@Singleton
public class MenuRailElement implements NavigationRailElement<MenuRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail", "menu-rail");
    private final MenuRailItemFactory factory;
    private final List<MenuRailItemElement> children = new LinkedList<>();

    @Inject MenuRailElement(MenuRailItemFactory factory, MenuHover hover) {
        this.factory = factory;
        // 초기 가시성은 HIDE. [mobile] 을 mode 구독보다 먼저 설정하지 않으면 BehaviorSubject
        // 의 즉시 emit 으로 expand() 가 호출되어 한 프레임 동안 desktop [expand] 레이아웃
        // (좌측 컬럼) 이 노출된 뒤 [mobile] 이 붙어 하단 바로 점프하는 flash 가 생긴다.
        element().setAttribute("hide", true);
        
        on(EventType.mouseleave, e -> hover.next(null));
    }

    private static final Comparator<Menu> MENU_COMPARATOR = nullsLast(comparing((Menu i) -> TRUE.equals(i.bottom())).thenComparing(Menu::order));

    public void update(List<Menu> menu) {
        clear();
        if(menu == null) return;
        // appBarSlot 이 지정된 메뉴는 {@link ShellAppBarElement} 가 AppBar slot 으로 승격해
        // 렌더하므로 MenuRail 에서는 제외한다 (세션 액션·전역 액션 성격 구분).
        menu.stream()
                .filter(m -> m.appBarSlot() == null)
                .sorted(MENU_COMPARATOR)
                .map(this::createItem)
                .forEach(this::add);
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

    public void setMode(MenuRailState state) {
        switch (state) {
            case EXPAND -> expand();
            case COLLAPSE -> collapse();
            case HIDE -> hide();
        }
    }

    public void setMobile(boolean mobile) {
        if (mobile) element().setAttribute("mobile", true);
        else element().removeAttribute("mobile");
    }
}

