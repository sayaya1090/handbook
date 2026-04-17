package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.TabsElementBuilder.PrimaryTabElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.htmlContainer;

/**
 * 도메인 객체({@link Menu} / {@link Tool}) → 네비게이션 엔트리 DOM 변환 **팩토리**.
 *
 * <p><b>책임 (얇은 매핑 전용):</b> 도메인 필드(title/icon 등)를 {@link MenuTabDecorator} 의
 * factory/fluent API 에 1:1 대응시키는 매핑만 수행한다. DOM 조립·아이콘 슬롯·라벨 i18n·click
 * 바인딩·tooltip/highlight 같은 **시각/동작 구조는 전부 {@link MenuTabDecorator} 가 담당**한다.
 * 여기서는 "어떤 Subject 로 발행할지"(MenuSelected vs ToolSelected), "어떤 data-* 키를 붙일지"
 * (menuTitle vs toolTitle), "어떤 호스트 종류를 쓸지"(primary-tab vs overflow menu-item) 만
 * 도메인 용어로 선언적으로 표현.</p>
 *
 * <p><b>SOLID:</b>
 * <ul>
 *   <li>S: 도메인 → Decorator 매핑만 담당. 레이아웃 결정(언제 overflow 로 이동시킬지) 은
 *       {@link MobileTabsElement} 가, 시각 구조 조립은 {@link MenuTabDecorator} 가 별도로 소유.</li>
 *   <li>O: 새 엔트리 종류가 생기면 Decorator 에 factory 를 추가하고 여기에 매핑 메서드를
 *       얇게 덧붙이면 된다.</li>
 *   <li>D: 호출측({@link MobileTabsElement})은 도메인 대응 메서드({@link #populateMenuTab} 등)에만 의존.</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuSelected} / {@link ToolSelected} — click 시 선택 발행 (Subject 구독자들이
 *       script 주입·active 토글 등 사이드 이펙트 처리)</li>
 *   <li>{@link LabelProvider} — i18n 라벨 구독</li>
 *   <li>{@link MenuTabDecorator} — 실제 시각 구조 조립 위임</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 이 클래스는 상태 없이 주입된 Subject/LabelProvider 만 포획한다 — 단일 Dagger
 * 싱글턴으로 충분. 본체 로직은 3개의 짧은 매핑 메서드뿐이고, 무게는 모두 {@link MenuTabDecorator}
 * 에 있다.</p>
 */
@Singleton
public class NavEntryFactory {

    private final MenuSelected selected;
    private final ToolSelected toolSelected;
    private final LabelProvider labelProvider;

    @Inject
    NavEntryFactory(MenuSelected selected, ToolSelected toolSelected, LabelProvider labelProvider) {
        this.selected = selected;
        this.toolSelected = toolSelected;
        this.labelProvider = labelProvider;
    }

    /**
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 를 {@link Menu} 용 네비게이션 탭으로
     * 데코레이트한다. tb 는 호출 시점에 이미 parent md-tabs 에 attach 되어 있다 (sayaya-ui
     * tab() 시맨틱). click 시 {@link MenuSelected} 발행.
     *
     * @return 편의상 데코레이트된 tab 의 native element. MobileTabsElement 가 detach/re-attach 에 사용.
     */
    public HTMLElement populateMenuTab(PrimaryTabElementBuilder tb, Menu menu) {
        return MenuTabDecorator.forPrimaryTab(tb, menu.icon())
                .dataset("menuTitle", menu.title())
                .i18n(menu.title(), labelProvider)
                .onClick(() -> selected.next(menu))
                .element();
    }

    /**
     * overflow 팝업(md-menu) 내부에서 사용할 {@code md-menu-item} 엔트리를 생성한다. click 시
     * {@link MenuSelected} 발행 후 {@code afterSelect} 콜백(일반적으로 overflow.close) 을 실행.
     */
    public HTMLElement renderMenuItem(Menu menu, Runnable afterSelect) {
        HTMLContainerBuilder<HTMLElement> mi = htmlContainer("md-menu-item", HTMLElement.class);
        return MenuTabDecorator.forOverflowMenuItem(mi, menu.icon())
                .dataset("menuTitle", menu.title())
                .i18n(menu.title(), labelProvider)
                .onClick(() -> {
                    selected.next(menu);
                    if (afterSelect != null) afterSelect.run();
                })
                .element();
    }

    /**
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 를 {@link Tool} 용 네비게이션 탭으로
     * 데코레이트한다 — MobileTabs 가 도구 모드일 때 사용. click 시 {@link ToolSelected} 발행.
     *
     * @return 편의상 데코레이트된 tab 의 native element.
     */
    public HTMLElement populateToolTab(PrimaryTabElementBuilder tb, Tool tool) {
        return MenuTabDecorator.forPrimaryTab(tb, tool.icon())
                .css("tool-tab")
                .dataset("toolTitle", tool.title())
                .i18n(tool.title(), labelProvider)
                .onClick(() -> toolSelected.next(tool))
                .element();
    }
}
