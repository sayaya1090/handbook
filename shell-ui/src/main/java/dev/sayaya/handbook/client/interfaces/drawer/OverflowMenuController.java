package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.MenuElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * {@link MobileTabsElement} 3단계 폴백의 "overflow" 슬롯을 책임지는 UI 컨트롤러.
 *
 * <p><b>책임:</b> sayaya-ui {@link IconButtonElementBuilder} + {@link MenuElementBuilder} 로
 * md-icon-button · md-menu 팝업의 DOM 소유와 open/close/visibility 를 제어 — Tabs 렌더 로직과
 * 분리해 {@link MobileTabsElement} 가 레이아웃 결정(탭 폭 측정,
 * {@link dev.sayaya.handbook.client.usecase.ResponsiveOverflow} 호출) 에만 집중할 수 있도록
 * 한다 (SRP).</p>
 *
 * <p><b>SOLID 반영:</b>
 * <ul>
 *   <li>S: overflow UI 의 DOM/이벤트/상태(open, hidden, children) 단일 책임.</li>
 *   <li>D: {@link MobileTabsElement} 는 이 컨트롤러의 추상(addItem/removeItem/setHidden
 *       /close) 에만 의존하며, 구체 {@code md-icon-button}/{@code md-menu} 웹컴포넌트와
 *       직접 상호작용하지 않는다.</li>
 *   <li>O: overflow UI 의 내부 변경(아이콘 교체·positioning 전략 등) 은 이 클래스 안에서
 *       완결되며 소비자 측 수정 불필요.</li>
 * </ul></p>
 *
 * <p><b>DOM 구조:</b> {@code <span class="menu-tabs-overflow"><md-icon-button>…</md-icon-button>
 * <md-menu>…</md-menu></span>} — 단일 wrapper 로 노출해 부모 flex 레이아웃에서 하나의
 * trailing cell 로 취급된다. 버튼이 {@code [hidden]} 일 때 wrapper 는 폭 0 으로 축소.
 * {@link MenuElementBuilder#anchorElement(HTMLElement)} 가 버튼 click 에 toggle 리스너를
 * 자동 등록하므로 별도 click 바인딩 불필요.</p>
 */
@Singleton
public class OverflowMenuController implements IsElement<HTMLElement> {

    private static final String BTN_ID = "menu-tabs-overflow-btn";

    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("menu-tabs-overflow");
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder button =
            new IconButtonElementBuilder.PlainIconButtonElementBuilder()
                    .css("menu-tabs-overflow-btn")
                    .attr("id", BTN_ID)
                    .ariaLabel("More")
                    .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-ellipsis"));
    private final MenuElementBuilder.TopMenuElementBuilder menu =
            MenuElementBuilder.menu()
                    .css("menu-tabs-overflow-menu")
                    .anchorElement(button.element());

    @Inject
    OverflowMenuController() {
        // md-menu 의 close-menu 이벤트(md-menu-item 선택 or 외부 클릭) 는 웹컴포넌트 커스텀
        // 이벤트라 EventType enum 에 없음 — 직접 리스너로 open 속성 제거. sayaya-ui 가 이
        // 커스텀 이벤트를 typed API 로 노출하면 교체. anchorElement(button) 가 click 에
        // toggle 리스너를 자동 등록하므로 별도 click 바인딩은 없다.
        menu.element().addEventListener("close-menu", e -> menu.close());
        _this.add(button).add(menu);
        setHidden(true);
    }

    public void setHidden(boolean hidden) {
        button.hidden(hidden);
    }

    public void close() {
        menu.close();
    }

    public void addItem(HTMLElement item) {
        menu.element().appendChild(item);
    }

    public void removeItem(HTMLElement item) {
        if (item.parentNode != null) item.parentNode.removeChild(item);
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}
