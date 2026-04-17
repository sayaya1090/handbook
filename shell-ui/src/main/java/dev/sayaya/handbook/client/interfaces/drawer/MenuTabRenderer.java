package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
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
 *       {@link #renderTab(Menu)} / {@link #renderMenuItem(Menu, Runnable)} 추상에만 의존.</li>
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
    private final LabelProvider labelProvider;

    @Inject
    MenuTabRenderer(MenuSelected selected, LabelProvider labelProvider) {
        this.selected = selected;
        this.labelProvider = labelProvider;
    }

    /**
     * {@code md-primary-tab} 1건을 생성한다. outline/filled 두 아이콘을 slot="icon" /
     * slot="active-icon" 에 배치해 MD3 활성 전환을 웹컴포넌트가 자동 처리하게 한다.
     * agent-command highlight 수신 시 {@link TooltipCard} 로 라벨 강조.
     */
    public HTMLElement renderTab(Menu menu) {
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
        // agent-command highlight 수신 시 tooltip 으로 라벨 강조 (hover 는 MD3 기본 동작으로 커버).
        final TooltipCard tooltip = TooltipCard.anchor(tab.element()).position("bottom").enabled(false);
        HighlightEffect.observe(tab.element(), () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        labelProvider.subscribe(labels -> {
            String title = labels.getOrDefault(menu.title(), menu.title() != null ? menu.title() : "");
            label.textContent = title;
            tooltip.content(title, null);
        });
        return tab.element();
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
}
