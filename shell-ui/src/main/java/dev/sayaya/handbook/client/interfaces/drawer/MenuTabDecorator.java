package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.components.HighlightEffect;
import dev.sayaya.handbook.client.components.TooltipCard;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.TabsElementBuilder.PrimaryTabElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * shell-ui 의 네비게이션 엔트리(모바일 탭, overflow 팝업 아이템) 조립 유틸. 호스트 엘리먼트
 * (md-primary-tab / md-menu-item) 별로 한 개의 정적 팩토리를 제공하고, 호출 한 번에 아이콘·라벨·
 * i18n 구독·click 바인딩·tooltip/highlight 연결을 모두 끝내 {@link HTMLElement} 하나만 반환한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>아이콘 슬롯 배치 — 탭은 outline/filled 쌍(slot=icon / active-icon), 메뉴 아이템은
 *       leading 아이콘 하나(slot=start)</li>
 *   <li>라벨 요소 삽입 + i18n 구독 바인딩 — 탭은 .menu-tab-label span, 메뉴 아이템은
 *       slot=headline div</li>
 *   <li>TooltipCard + HighlightEffect 연결 — 탭에만 적용 (메뉴 아이템은 팝업 내부라 불필요)</li>
 *   <li>click 핸들러 연결 — 호스트 빌더 종류에 맞는 on(EventType.click, ...) 경로</li>
 *   <li>data-* 속성 부여 — agent selector 용 marker (menuTitle / toolTitle)</li>
 * </ul></p>
 *
 * <p><b>설계 배경:</b> 초기에는 fluent 데코레이터(.css/.dataset/.i18n/.onClick/.element) 로
 * 시작했으나, 호출 지점이 {@link NavEntryFactory} 의 3개 매핑 메서드뿐이고 모두 같은 패턴으로
 * 전 메서드를 순차 호출했기에 fluent 자체가 간접성만 추가했다. 정적 팩토리 + 파라미터 노출로
 * 퇴화시켜 인스턴스/체인을 제거 — 호출부는 한 줄 팩토리 호출로 끝난다.</p>
 *
 * <p><b>주의:</b> {@link #primaryTab} 호출 시점에 탭 빌더는 이미 parent md-tabs 에 attach
 * 되어 있어야 한다 (sayaya-ui {@code tab()} 시맨틱). 이 유틸은 호스트에 자식 DOM 추가 +
 * 이벤트 연결만 수행하며 부모에 마운트하지 않는다.</p>
 */
final class MenuTabDecorator {
    private MenuTabDecorator() {
        // 유틸 클래스 — 인스턴스화 금지.
    }

    /**
     * {@code md-primary-tab} 호스트를 한 번에 조립해 native element 를 반환한다. outline(slot=icon)
     * + filled(slot=active-icon) 두 아이콘을 대칭 배치하고 라벨 span, tooltip, highlight observer,
     * click 핸들러, i18n 구독을 연결한다. MD3 md-primary-tab 이 active 속성 토글에 따라 두 슬롯을
     * 자동 교체 렌더 + indicator 이동 애니메이션을 처리.
     *
     * @param tb          데코레이트 대상 탭 빌더 (이미 parent md-tabs 에 attach 된 상태여야 함)
     * @param faIcon      FontAwesome 아이콘 클래스 (fa-xxx)
     * @param datasetKey  agent selector 용 data-* 키 이름 (menuTitle / toolTitle)
     * @param i18nKey     LabelProvider 해석 키. null 이면 dataset/라벨 모두 skip
     * @param provider    라벨 해석용 LabelProvider
     * @param onClick     탭 click 시 실행할 핸들러
     * @param extraCss    추가 CSS 클래스 (tool 모드 탭은 {@code tool-tab})
     * @return 조립 완료된 md-primary-tab 의 native element — 호출측이 detach/re-attach·active 토글에 사용
     */
    static HTMLElement primaryTab(PrimaryTabElementBuilder tb, String faIcon,
                                  String datasetKey, String i18nKey,
                                  LabelProvider provider, Runnable onClick,
                                  String... extraCss) {
        tb.css("menu-tab");
        for (String c : extraCss) tb.element().classList.add(c);
        tb.add(IconElementBuilder.icon().css("fa-sharp", "fa-light", faIcon, "icon-outline")
                        .attr("slot", "icon"))
                .add(IconElementBuilder.icon().css("fa-sharp", "fa-solid", faIcon, "icon-filled")
                        .attr("slot", "active-icon"));
        HTMLElement tab = tb.element();
        // elemento tb.text() 는 textContent 덮어쓰기라 named slot 자식(md-icon) 까지 삭제되므로
        // label 은 별도 span 에 두어 i18n 갱신이 slot 자식에 영향 주지 않게 한다.
        HTMLElement label = span().css("menu-tab-label").element();
        tab.appendChild(label);
        if (i18nKey != null) tab.dataset.set(datasetKey, i18nKey);
        tb.on(EventType.click, evt -> onClick.run());
        TooltipCard tooltip = TooltipCard.anchor(tab).position("bottom").enabled(false);
        HighlightEffect.observe(tab,
                () -> tooltip.showImmediate(TooltipCard.AUTO_HIDE_HIGHLIGHT_MS));
        provider.subscribe(labels -> {
            String title = labels.getOrDefault(i18nKey, i18nKey != null ? i18nKey : "");
            label.textContent = title;
            tooltip.content(title, null);
        });
        return tab;
    }

    /**
     * {@code md-menu-item} 호스트를 한 번에 조립해 native element 를 반환한다. leading 아이콘
     * (slot=start) + 라벨(slot=headline) 만 배치. overflow 팝업 내부 엔트리라 tooltip/highlight 는
     * 적용하지 않는다.
     *
     * @param mi          데코레이트 대상 md-menu-item 빌더
     * @param faIcon      FontAwesome 아이콘 클래스
     * @param datasetKey  agent selector 용 data-* 키 이름
     * @param i18nKey     LabelProvider 해석 키. null 이면 dataset/라벨 모두 skip
     * @param provider    라벨 해석용 LabelProvider
     * @param onClick     아이템 click 시 실행할 핸들러
     * @return 조립 완료된 md-menu-item 의 native element
     */
    static HTMLElement overflowMenuItem(HTMLContainerBuilder<HTMLElement> mi, String faIcon,
                                        String datasetKey, String i18nKey,
                                        LabelProvider provider, Runnable onClick) {
        mi.css("menu-tab-menu-item");
        HTMLElement icon = IconElementBuilder.icon()
                .css("fa-sharp", "fa-light", faIcon, "icon-outline")
                .attr("slot", "start").element();
        mi.element().appendChild(icon);
        HTMLElement headline = div().element();
        headline.setAttribute("slot", "headline");
        mi.element().appendChild(headline);
        if (i18nKey != null) mi.element().dataset.set(datasetKey, i18nKey);
        mi.on(EventType.click, evt -> onClick.run());
        provider.subscribe(labels -> {
            String title = labels.getOrDefault(i18nKey, i18nKey != null ? i18nKey : "");
            headline.textContent = title;
        });
        return mi.element();
    }
}
