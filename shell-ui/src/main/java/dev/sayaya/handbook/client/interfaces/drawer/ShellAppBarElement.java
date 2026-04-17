package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
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
 * 모바일 뷰포트 전용 상단 AppBar.
 *
 * <p><b>책임:</b> MD3 Top App Bar (Small variant) semantic 으로 컨텍스트 정보 + 전역 액션
 * (햄버거 / 워크스페이스 셀렉터 / 테마 토글 등) 을 상단에 집약한다. {@link MobileTabsElement}
 * 는 이 AppBar 바로 아래에 stack 되어 네비게이션 축(탭)과 전역 액션 축(AppBar) 이 MD3
 * 관용대로 분리된다.</p>
 *
 * <p><b>구조 (3 slot):</b>
 * <ul>
 *   <li>{@code .shell-app-bar-leading} — {@link MenuToggleButton} 등 네비게이션 아이콘</li>
 *   <li>{@code .shell-app-bar-center} — 워크스페이스 셀렉터 / 현재 컨텍스트 제목 등</li>
 *   <li>{@code .shell-app-bar-trailing} — {@link ThemeToggle} / 세션 액션 / overflow 등</li>
 * </ul>
 * 실제 element 이동(mount/unmount) 은 {@link DrawerElement} 가 {@link dev.sayaya.handbook.usecase.ViewportObserver}
 * 구독으로 처리한다. ShellAppBarElement 자체는 slot 제공자 역할만 한다.</p>
 *
 * <p><b>주의:</b> 데스크톱에서는 DrawerElement 가 {@code [hide]} 속성을 걸어 숨긴다
 * (CSS 에서 추가 2차 방어). 데스크톱 AppBar 로의 확장(agent input center 편입) 은 후속 커밋.</p>
 */
@Singleton
public class ShellAppBarElement implements IsElement<HTMLElement> {

    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = htmlContainer("header", HTMLElement.class).css("shell-app-bar");
    private final HTMLContainerBuilder<HTMLDivElement> leading = div().css("shell-app-bar-leading");
    private final HTMLContainerBuilder<HTMLDivElement> center = div().css("shell-app-bar-center");
    private final HTMLContainerBuilder<HTMLDivElement> trailing = div().css("shell-app-bar-trailing");

    private final MenuSelected selected;
    private final LabelProvider labelProvider;
    /** MenuList 에서 appBarSlot={@code "trailing"} 으로 승격된 엔트리들. 재렌더링 시 DOM 분리. */
    private final List<MenuActionEntry> trailingActions = new LinkedList<>();

    @Inject
    ShellAppBarElement(MenuList list, MenuSelected selected, LabelProvider labelProvider) {
        this.selected = selected;
        this.labelProvider = labelProvider;
        _this.add(leading).add(center).add(trailing);
        element().setAttribute("hide", true);
        list.distinctUntilChanged().subscribe(this::updateMenuActions);
    }

    private void updateMenuActions(List<Menu> menus) {
        // 기존 엔트리 detach (정적 slot 에 DrawerElement 가 꽂은 element 는 건드리지 않음).
        for (MenuActionEntry e : trailingActions) {
            if (e.element.parentNode != null) e.element.parentNode.removeChild(e.element);
        }
        trailingActions.clear();
        if (menus == null) return;
        for (Menu m : menus) {
            if (!"trailing".equals(m.appBarSlot())) continue;
            MenuActionEntry entry = createActionButton(m);
            trailingActions.add(entry);
            trailing.element().insertBefore(entry.element, trailing.element().firstChild);
        }
    }

    /**
     * appBarSlot 승격 메뉴 1건을 {@code md-icon-button} 으로 렌더한다. 클릭 시 {@link MenuSelected}
     * 에 발행 — 기존 네비게이션 경로(MenuRailItemElement)와 동일한 선택 이벤트로 수렴된다.
     */
    private MenuActionEntry createActionButton(Menu menu) {
        HTMLContainerBuilder<HTMLElement> btn = htmlContainer("md-icon-button", HTMLElement.class)
                .css("shell-app-bar-action");
        HTMLElement icon = IconElementBuilder.icon()
                .css("fa-sharp", "fa-solid", menu.icon()).element();
        btn.add(icon);
        if (menu.title() != null) {
            btn.element().dataset.set("menuTitle", menu.title());
            labelProvider.subscribe(labels -> {
                String title = labels.getOrDefault(menu.title(), menu.title());
                btn.element().setAttribute("aria-label", title);
                btn.element().setAttribute("title", title);
            });
        }
        btn.on(EventType.click, evt -> selected.next(menu));
        return new MenuActionEntry(menu, btn.element());
    }

    /** leading slot — DrawerElement 가 햄버거를 여기로 이동한다. */
    public HTMLElement leadingSlot() { return leading.element(); }

    /** center slot — DrawerElement 가 WorkspaceSelect 등을 여기로 이동한다. */
    public HTMLElement centerSlot() { return center.element(); }

    /** trailing slot — DrawerElement 가 ThemeToggle 등을 여기로 이동한다. */
    public HTMLElement trailingSlot() { return trailing.element(); }

    @Override
    public HTMLElement element() { return _this.element(); }

    private static class MenuActionEntry {
        final Menu menu;
        final HTMLElement element;
        MenuActionEntry(Menu menu, HTMLElement element) {
            this.menu = menu;
            this.element = element;
        }
    }
}
