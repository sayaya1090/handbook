package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.TabsElementBuilder.PrimaryTabElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlContainer;
import static org.jboss.elemento.Elements.span;

/**
 * 단일 {@link Menu} 를 모바일 네비게이션 DOM (md-primary-tab / md-menu-item) 으로 렌더링하는
 * 팩토리.
 *
 * <p><b>책임:</b> {@link MobileTabsElement} 가 소유하던 DOM 조립 로직을 분리해 단일 책임으로
 * 수용한다 (SRP). 아이콘(outline/filled) 슬롯 배치, 라벨·i18n 바인딩, click 핸들러, TooltipCard
 * + HighlightEffect 연결까지 이 클래스 안에서 완결.</p>
 *
 * <p><b>SOLID 반영:</b>
 * <ul>
 *   <li>S: 메뉴 → DOM 렌더만 담당. 레이아웃 결정(언제 overflow 로 이동시킬지) 은 호출측
 *       {@link MobileTabsElement} 가 별도 판정.</li>
 *   <li>O: 새 렌더 변형(예: leading slot 전용 mini tab) 이 필요하면 메서드 추가만 하면 된다.</li>
 *   <li>D: 호출측은 구체적인 {@code md-primary-tab}/{@code md-menu-item} DOM 을 조립하지 않고
 *       {@link #populateMenuTab} / {@link #renderMenuItem} 추상에만 의존.</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuSelected} — click 시 선택 발행</li>
 *   <li>{@link LabelProvider} — i18n 라벨 구독</li>
 * </ul></p>
 */
@Singleton
public class MenuTabRenderer {

    private final MenuSelected selected;
    private final ToolSelected toolSelected;
    private final LabelProvider labelProvider;

    @Inject
    MenuTabRenderer(MenuSelected selected, ToolSelected toolSelected, LabelProvider labelProvider) {
        this.selected = selected;
        this.toolSelected = toolSelected;
        this.labelProvider = labelProvider;
    }

    /**
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 에 menu 렌더 내용을 주입한다.
     * tb 는 호출 시점에 이미 parent md-tabs 에 attach 되어 있다 (sayaya-ui tab() 시맨틱).
     * outline/filled 두 아이콘을 slot="icon" / slot="active-icon" 에 배치해 MD3 활성 전환을
     * 웹컴포넌트가 자동 처리하게 한다. agent-command highlight 수신 시 {@link TooltipCard} 로
     * 라벨 강조.
     *
     * @return 편의상 주입된 tab 의 native element. MobileTabsElement 가 detach/re-attach 용도로 보관.
     */
    public HTMLElement populateMenuTab(PrimaryTabElementBuilder tb, Menu menu) {
        // tb.icon(...) 은 sayaya-ui HasIconSlot 이 slot="icon" 을 자동 설정 + add.
        // slot="active-icon" 은 sayaya-ui 가 별도 슬롯 API 를 노출하지 않으므로 수동 append.
        tb.css("menu-tab").icon(
                IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon(), "icon-outline"));
        HTMLElement tab = tb.element();
        HTMLElement iconFilled = IconElementBuilder.icon()
                .css("fa-sharp", "fa-solid", menu.icon(), "icon-filled").element();
        iconFilled.setAttribute("slot", "active-icon");
        tab.appendChild(iconFilled);
        HTMLElement label = span().css("menu-tab-label").element();
        tab.appendChild(label);
        if (menu.title() != null) tab.dataset.set("menuTitle", menu.title());
        tb.on(EventType.click, evt -> selected.next(menu));
        // agent-command highlight 수신 시 tooltip 으로 라벨 강조 (hover 는 MD3 기본 동작으로 커버).
        final TooltipCard tooltip = TooltipCard.anchor(tab).position("bottom").enabled(false);
        HighlightEffect.observe(tab, () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            label.textContent = title;
            tooltip.content(title, null);
        });
        return tab;
    }

    /**
     * {@code md-menu-item} 1건을 생성한다 — overflow 팝업(md-menu) 내부 엔트리 용도. click 시
     * {@link MenuSelected} 발행 후 {@code afterSelect} 콜백(일반적으로 overflow.close) 을 실행.
     */
    public HTMLElement renderMenuItem(Menu menu, Runnable afterSelect) {
        HTMLContainerBuilder<HTMLElement> mi = htmlContainer("md-menu-item", HTMLElement.class).css("menu-tab-menu-item");
        HTMLElement miIcon = IconElementBuilder.icon()
                .css("fa-sharp", "fa-light", menu.icon(), "icon-outline").element();
        miIcon.setAttribute("slot", "start");
        mi.add(miIcon);
        HTMLElement headline = div().element();
        headline.setAttribute("slot", "headline");
        mi.add(headline);
        if (menu.title() != null) mi.element().dataset.set("menuTitle", menu.title());
        mi.on(EventType.click, evt -> {
            selected.next(menu);
            if (afterSelect != null) afterSelect.run();
        });
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            headline.textContent = title;
        });
        return mi.element();
    }

    /**
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 에 tool 렌더 내용을 주입한다 — 모바일
     * MobileTabs 가 도구 모드일 때 사용. tb 는 호출 시점에 이미 parent md-tabs 에 attach 된 상태.
     * click 시 {@link ToolSelected} 발행. Menu 의 populateMenuTab 과 동일한 outline/filled 아이콘
     * 슬롯 + 라벨 구조.
     *
     * @return 편의상 주입된 tab 의 native element. MobileTabsElement 가 detach 용도로 보관.
     */
    public HTMLElement populateToolTab(PrimaryTabElementBuilder tb, Tool tool) {
        tb.css("menu-tab", "tool-tab").icon(
                IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon(), "icon-outline"));
        HTMLElement tab = tb.element();
        HTMLElement iconFilled = IconElementBuilder.icon()
                .css("fa-sharp", "fa-solid", tool.icon(), "icon-filled").element();
        iconFilled.setAttribute("slot", "active-icon");
        tab.appendChild(iconFilled);
        HTMLElement label = span().css("menu-tab-label").element();
        tab.appendChild(label);
        if (tool.title() != null) tab.dataset.set("toolTitle", tool.title());
        tb.on(EventType.click, evt -> toolSelected.next(tool));
        final TooltipCard tooltip = TooltipCard.anchor(tab).position("bottom").enabled(false);
        HighlightEffect.observe(tab, () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(tool.title(), tool.title() != null ? tool.title() : "");
            label.textContent = title;
            tooltip.content(title, null);
        });
        return tab;
    }
}
