package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlContainer;

/**
 * 데스크톱/모바일 공통 상단 AppBar.
 *
 * <p><b>책임:</b> MD3 Top App Bar (Small variant) semantic 으로 컨텍스트 정보 + 전역 액션
 * (워크스페이스 셀렉터 / 테마 토글 / appBarSlot 승격 메뉴 등) 을 상단에 집약한다.
 * {@link MobileTabsElement} 는 이 AppBar 바로 아래에 stack 되어 네비게이션 축(탭)과
 * 전역 액션 축(AppBar) 이 MD3 관용대로 분리된다.</p>
 *
 * <p><b>구조 (3 slot):</b>
 * <ul>
 *   <li>{@code .shell-app-bar-leading} — 좌측 예비 슬롯 (현재 정적 엔트리 없음; 동적 메뉴가
 *       {@code appBarSlot="leading"} 으로 승격 가능). 햄버거 토글은 MenuRail 상단으로
 *       이동되었다 — {@link MenuRailElement} 참조.</li>
 *   <li>{@code .shell-app-bar-center} — 워크스페이스 셀렉터 / 현재 컨텍스트 제목 등</li>
 *   <li>{@code .shell-app-bar-trailing} — {@link ThemeToggle} / 세션 액션 / overflow 등</li>
 * </ul></p>
 *
 * <p><b>햄버거 위치 이력(2026-04):</b> AppBar 도입 초기엔 햄버거를 leading 에 두었으나
 * AppBar 가 {@code left: var(--shell-drawer-width)} 로 Drawer 오른쪽부터 시작하는 탓에
 * rail expand 상태에서 햄버거가 시각적으로 우측으로 밀리는 증상이 발생. MD3 Navigation
 * Rail 정석(레일 상단 toggle)으로 복귀해 햄버거를 {@link MenuRailElement} 상단에 mount 한다.</p>
 */
@Singleton
public class ShellAppBarElement implements IsElement<HTMLElement> {

    /** {@link Menu#appBarSlot()} 값과 매칭되는 표준 slot 이름. O/C 원칙 — 신규 slot 추가는 맵 확장만. */
    public static final String SLOT_LEADING = "leading";
    public static final String SLOT_CENTER = "center";
    public static final String SLOT_TRAILING = "trailing";

    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = htmlContainer("header", HTMLElement.class).css("shell-app-bar");
    private final HTMLContainerBuilder<HTMLDivElement> leading = div().css("shell-app-bar-leading");
    private final HTMLContainerBuilder<HTMLDivElement> center = div().css("shell-app-bar-center");
    private final HTMLContainerBuilder<HTMLDivElement> trailing = div().css("shell-app-bar-trailing");
    /** slot 이름 → 실제 HTMLElement 매핑. 신규 slot 확장은 이 맵에 항목 추가만 필요 (O/C). */
    private final Map<String, HTMLElement> slots = new LinkedHashMap<>();

    private final MenuSelected selected;
    private final LabelProvider labelProvider;
    /** MenuList 에서 appBarSlot 으로 승격된 엔트리들. 재렌더링 시 DOM 분리. */
    private final List<MenuActionEntry> menuActions = new LinkedList<>();

    @Inject
    ShellAppBarElement(MenuList list, MenuSelected selected, LabelProvider labelProvider,
                       WorkspaceSelectElement workspace, ThemeToggle themeToggle) {
        this.selected = selected;
        this.labelProvider = labelProvider;
        _this.add(leading).add(center).add(trailing);
        slots.put(SLOT_LEADING, leading.element());
        slots.put(SLOT_CENTER, center.element());
        slots.put(SLOT_TRAILING, trailing.element());
        // AppBar 는 데스크톱/모바일 공통 상시 표시 — 별도 초기 [hide] 속성 불필요.
        // SRP — AppBar 가 자기 정적 slot 을 스스로 채운다. DrawerElement 는 슬롯 지식 없음.
        // 햄버거(MenuToggleButton)는 MenuRail 상단에 mount — AppBar left 오프셋 회귀 회피.
        center.element().appendChild(workspace.css("workspace").element());
        // 정적 엔트리에도 data-order 를 부여해 동적 메뉴와 단일 정렬 기준(order 오름차순,
        // 우선순위 높을수록 우측) 으로 통합 정렬된다. 테마 토글은 중간 우선순위("M") —
        // 세션 액션(SIGN_IN/OUT, order="Z") 은 항상 테마 토글의 오른쪽에 놓인다.
        themeToggle.element().dataset.set("appBarOrder", "M");
        trailing.element().appendChild(themeToggle.element());
        list.distinctUntilChanged().subscribe(this::updateMenuActions);
        // MD3 Small Top App Bar: scroll=0 → Surface 기본 / scroll>0 → Surface-container
        // + elevation 으로 전환. window scroll 이벤트 동기화해 [scrolled] 속성 토글.
        DomGlobal.window.addEventListener("scroll", e -> updateScrolled());
        updateScrolled();
    }

    /**
     * 현재 {@code window.scrollY} 기준으로 AppBar 의 {@code [scrolled]} 속성과 {@code body}
     * 의 {@code [data-scrolled]} 속성을 함께 on/off. body 속성은 MobileTabs 등 다른 상단
     * bar 가 CSS 만으로 동일 scroll state 에 반응하도록 하는 공용 앵커.
     */
    private void updateScrolled() {
        boolean scrolled = DomGlobal.window.scrollY > 0;
        if (scrolled) {
            element().setAttribute("scrolled", true);
            DomGlobal.document.body.setAttribute("data-scrolled", "true");
        } else {
            element().removeAttribute("scrolled");
            DomGlobal.document.body.removeAttribute("data-scrolled");
        }
    }

    private void updateMenuActions(List<Menu> menus) {
        // 기존 동적 엔트리 detach (정적 주입 element 는 건드리지 않음).
        for (MenuActionEntry e : menuActions) {
            if (e.element.parentNode != null) e.element.parentNode.removeChild(e.element);
        }
        menuActions.clear();
        if (menus == null) return;
        // 동적 메뉴를 slot 별로 생성 후 append. slot 내 정렬은 reorderSlot 이 data-app-bar-order
        // (정적 엔트리도 포함) 오름차순 기준으로 재배치한다 — 오름차순일수록 왼쪽.
        for (Menu m : menus) {
            HTMLElement slot = slots.get(m.appBarSlot());
            if (slot == null) continue; // null 이거나 미지원 slot — 스킵.
            MenuActionEntry entry = createActionButton(m);
            entry.element.dataset.set("appBarOrder", m.order() != null ? m.order() : "");
            menuActions.add(entry);
            slot.appendChild(entry.element);
        }
        // 영향 받은 slot 만 재정렬. leading/center 에는 동적 엔트리가 거의 없으므로 저비용.
        menus.stream()
                .map(Menu::appBarSlot)
                .filter(slots::containsKey)
                .distinct()
                .forEach(name -> reorderSlot(slots.get(name)));
    }

    /**
     * 지정된 slot 내 모든 자식을 {@code data-app-bar-order} 속성 기준으로 오름차순 재배치한다.
     * 속성이 없거나 빈 값인 엔트리는 빈 문자열로 취급되어 가장 왼쪽에 위치. 정적 주입 엔트리와
     * 동적 메뉴가 단일 기준으로 섞여 정렬된다.
     */
    private static void reorderSlot(HTMLElement slot) {
        List<HTMLElement> sorted = new LinkedList<>();
        // childNodes 는 Text 포함이라 firstElementChild/nextElementSibling 으로 element 만 순회.
        elemental2.dom.Element cur = slot.firstElementChild;
        while (cur != null) {
            if (cur instanceof HTMLElement) sorted.add((HTMLElement) cur);
            cur = cur.nextElementSibling;
        }
        sorted = sorted.stream()
                .sorted(Comparator.comparing(el -> {
                    String v = el.dataset.get("appBarOrder");
                    return v != null ? v : "";
                }))
                .collect(Collectors.toList());
        for (HTMLElement el : sorted) slot.appendChild(el); // appendChild 는 기존 위치에서 이동
    }

    /**
     * appBarSlot 승격 메뉴 1건을 {@code md-icon-button} 으로 렌더한다. 클릭 시 {@link MenuSelected}
     * 에 발행 — 기존 네비게이션 경로(MenuRailItemElement)와 동일한 선택 이벤트로 수렴된다.
     */
    private MenuActionEntry createActionButton(Menu menu) {
        // sayaya-ui IconButtonElementBuilder — HasIconSlot.icon() + ariaLabel() + on(click) 체이닝.
        var btn = new IconButtonElementBuilder.PlainIconButtonElementBuilder()
                .css("shell-app-bar-action")
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", menu.icon()))
                .on(EventType.click, evt -> selected.next(menu));
        HTMLElement el = btn.element();
        // agent-command highlight 수신 시 TooltipCard 로 라벨 강조. hover 는 md-icon-button 의
        // 기본 aria/title 이 담당하므로 tooltip 은 highlight 전용 (enabled=false).
        final TooltipCard tooltip = TooltipCard.anchor(el).position("bottom").enabled(false);
        HighlightEffect.observe(el, () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        if (menu.title() != null) {
            el.dataset.set("menuTitle", menu.title());
            labelProvider.subscribe(labels -> {
                String title = labels.getOrDefault(menu.title(), menu.title());
                btn.ariaLabel(title);
                el.setAttribute("title", title);
                tooltip.content(title, null);
            });
        }
        return new MenuActionEntry(menu, el);
    }

    /** leading slot — 현재 정적 엔트리 없음. appBarSlot="leading" 으로 승격된 동적 메뉴 수용. */
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
