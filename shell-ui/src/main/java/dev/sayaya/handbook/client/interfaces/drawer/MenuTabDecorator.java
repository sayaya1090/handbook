package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.TabsElementBuilder.PrimaryTabElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * shell-ui 의 네비게이션 엔트리(모바일 탭, overflow 팝업 아이템)에 공통 부가 구조를 입히는
 * 데코레이터. 구체적인 호스트 엘리먼트(md-primary-tab / md-menu-item) 별 차이는 두 개의 static
 * factory 가 흡수하고, 이후 fluent API(dataset/i18n/onClick)는 호스트 종류와 무관하게 공유된다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>아이콘 슬롯 배치 — 탭은 outline/filled 쌍(slot=icon / active-icon), 메뉴 아이템은
 *       leading 아이콘 하나(slot=start)</li>
 *   <li>라벨 요소 삽입 + i18n 구독 바인딩 — 탭은 .menu-tab-label span, 메뉴 아이템은
 *       slot=headline div</li>
 *   <li>TooltipCard + HighlightEffect 연결 — 탭에만 적용 (메뉴 아이템은 팝업 내부라 불필요)</li>
 *   <li>click 핸들러 연결 — 호스트 빌더 종류에 맞는 on(EventType.click, ...) 경로를
 *       {@code clickBinder} 로 추상화</li>
 * </ul></p>
 *
 * <p><b>설계 배경:</b> 기존 {@link NavEntryFactory} (구 MenuTabRenderer) 의 세 메서드가 동일한
 * 조각(라벨 i18n, data-marker, click)을 복제하고 호스트 종류만 달랐다. 데코레이터로 공통 구조를
 * 묶고, 호스트별 초기 조립을 factory 에 분리해 호출부는 조립 의도만 드러나게 한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link PrimaryTabElementBuilder} — sayaya-ui Tabs 의 개별 primary 탭 빌더</li>
 *   <li>{@link IconElementBuilder} — FontAwesome 아이콘 md-icon 래퍼</li>
 *   <li>{@link TooltipCard}, {@link HighlightEffect} — agent-command 라벨 강조 (탭 전용)</li>
 *   <li>{@link LabelProvider} — i18n 키 → 현재 언어 라벨 해석</li>
 * </ul></p>
 *
 * <p><b>주의:</b> {@link #forPrimaryTab} 호출 시점에 탭 빌더는 이미 parent md-tabs 에 attach
 * 되어 있어야 한다 (sayaya-ui {@code tab()} 시맨틱). 데코레이터는 호스트에 자식 DOM 추가 +
 * 이벤트 연결만 수행하며, 부모에 마운트하지 않는다.</p>
 */
class MenuTabDecorator {
    private final HTMLElement host;
    private final HTMLElement label;
    private final TooltipCard tooltip;           // null 가능 — 메뉴 아이템 경로에서는 미적용.
    private final Consumer<Runnable> clickBinder; // 호스트 타입별 on(click) 경로 추상화.

    private MenuTabDecorator(HTMLElement host, HTMLElement label, TooltipCard tooltip,
                             Consumer<Runnable> clickBinder) {
        this.host = host;
        this.label = label;
        this.tooltip = tooltip;
        this.clickBinder = clickBinder;
    }

    /**
     * md-primary-tab 호스트용 팩토리. outline(slot=icon) + filled(slot=active-icon) 두 아이콘을
     * 대칭 배치하고 라벨 span, tooltip, highlight observer 를 연결한다. MD3 md-primary-tab 이
     * active 속성 토글에 따라 두 슬롯을 자동 교체 렌더 + indicator 이동 애니메이션을 처리.
     */
    static MenuTabDecorator forPrimaryTab(PrimaryTabElementBuilder tb, String faIcon) {
        tb.css("menu-tab")
                .add(IconElementBuilder.icon().css("fa-sharp", "fa-light", faIcon, "icon-outline")
                        .attr("slot", "icon"))
                .add(IconElementBuilder.icon().css("fa-sharp", "fa-solid", faIcon, "icon-filled")
                        .attr("slot", "active-icon"));
        HTMLElement tab = tb.element();
        // elemento tb.text() 는 textContent 덮어쓰기라 named slot 자식(md-icon) 까지 삭제되므로
        // label 은 별도 span 에 두어 i18n 갱신이 slot 자식에 영향 주지 않게 한다.
        HTMLElement label = span().css("menu-tab-label").element();
        tab.appendChild(label);
        TooltipCard tooltip = TooltipCard.anchor(tab).position("bottom").enabled(false);
        HighlightEffect.observe(tab,
                () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        return new MenuTabDecorator(tab, label, tooltip,
                handler -> tb.on(EventType.click, evt -> handler.run()));
    }

    /**
     * md-menu-item 호스트용 팩토리. leading 아이콘(slot=start) + 라벨(slot=headline) 만 배치.
     * overflow 팝업 내부 엔트리라 tooltip/highlight 는 적용하지 않는다.
     */
    static MenuTabDecorator forOverflowMenuItem(HTMLContainerBuilder<HTMLElement> mi, String faIcon) {
        mi.css("menu-tab-menu-item");
        HTMLElement icon = IconElementBuilder.icon()
                .css("fa-sharp", "fa-light", faIcon, "icon-outline")
                .attr("slot", "start").element();
        mi.element().appendChild(icon);
        HTMLElement headline = div().element();
        headline.setAttribute("slot", "headline");
        mi.element().appendChild(headline);
        return new MenuTabDecorator(mi.element(), headline, null,
                handler -> mi.on(EventType.click, evt -> handler.run()));
    }

    /** 호스트 element 에 추가 CSS 클래스 부여 (예: tool 모드 탭은 {@code tool-tab}). */
    MenuTabDecorator css(String... classes) {
        for (String c : classes) host.classList.add(c);
        return this;
    }

    /** 호스트 element 의 data-* 속성 부여 — agent selector 용 marker. value 가 null 이면 noop. */
    MenuTabDecorator dataset(String key, String value) {
        if (value != null) host.dataset.set(key, value);
        return this;
    }

    /**
     * i18n 키로 라벨을 구독한다. LabelProvider 가 현재 언어 라벨을 발행하면 label textContent 와
     * tooltip content 를 동시 갱신. 매핑 실패 시 키를 그대로 렌더. tooltip 은 탭 경로에만 존재.
     */
    MenuTabDecorator i18n(String key, LabelProvider provider) {
        provider.subscribe(labels -> {
            String title = labels.getOrDefault(key, key != null ? key : "");
            label.textContent = title;
            if (tooltip != null) tooltip.content(title, null);
        });
        return this;
    }

    /** 호스트 click 시 실행될 핸들러를 연결 — 호스트 빌더 타입별 on(click) 경로는 생성자에서 주입된 clickBinder 가 추상화. */
    MenuTabDecorator onClick(Runnable handler) {
        clickBinder.accept(handler);
        return this;
    }

    /** 데코레이트 된 호스트의 native element — 호출측이 detach/re-attach 또는 active 토글 등에 사용. */
    HTMLElement element() {
        return host;
    }
}
