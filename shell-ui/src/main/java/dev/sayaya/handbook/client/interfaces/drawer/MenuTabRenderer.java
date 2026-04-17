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
 * 단일 {@link Menu} / {@link Tool} 을 모바일 네비게이션 DOM (md-primary-tab / md-menu-item) 으로
 * 렌더링하는 팩토리.
 *
 * <p><b>책임:</b> {@link MobileTabsElement} 가 어떤 엔트리 종류를 렌더할지를 도메인 용어로
 * 선언적으로 표현한다. DOM 조립·아이콘 슬롯·라벨 i18n·click 핸들러·tooltip/highlight 연결은
 * {@link MenuTabDecorator} 가 담당하고, 여기서는 "어떤 도메인 객체를 어떤 호스트에 어떤
 * Subject 로 발행할지" 만 기술.</p>
 *
 * <p><b>SOLID 반영:</b>
 * <ul>
 *   <li>S: 메뉴/도구 → DOM 렌더만 담당. 레이아웃 결정(언제 overflow 로 이동시킬지) 은 호출측
 *       {@link MobileTabsElement} 가 별도 판정.</li>
 *   <li>O: 새 렌더 변형이 필요하면 {@link MenuTabDecorator} 에 factory/fluent 메서드를 추가할 뿐
 *       이 클래스는 도메인 조립 한 곳만 바꾼다.</li>
 *   <li>D: 호출측은 {@link #populateMenuTab} / {@link #renderMenuItem} / {@link #populateToolTab}
 *       추상에만 의존.</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuSelected} / {@link ToolSelected} — click 시 선택 발행 (Subject 구독자들이
 *       script 주입·active 토글 등 사이드 이펙트 처리)</li>
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
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 에 menu 탭 내용을 주입한다.
     * tb 는 호출 시점에 이미 parent md-tabs 에 attach 되어 있다 (sayaya-ui tab() 시맨틱).
     *
     * @return 편의상 주입된 tab 의 native element. MobileTabsElement 가 detach/re-attach 용도로 보관.
     */
    public HTMLElement populateMenuTab(PrimaryTabElementBuilder tb, Menu menu) {
        return MenuTabDecorator.forPrimaryTab(tb, menu.icon())
                .dataset("menuTitle", menu.title())
                .i18n(menu.title(), labelProvider)
                .onClick(() -> selected.next(menu))
                .element();
    }

    /**
     * overflow 팝업(md-menu) 내부 엔트리용 {@code md-menu-item} 을 생성한다. click 시
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
     * 주어진 sayaya-ui {@link PrimaryTabElementBuilder} 에 tool 탭 내용을 주입한다 — MobileTabs 가
     * 도구 모드일 때 사용. click 시 {@link ToolSelected} 발행.
     *
     * @return 편의상 주입된 tab 의 native element.
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
