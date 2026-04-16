package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.ToolList;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.jboss.elemento.Elements.div;

/**
 * 도구 레일 네비게이션 컨테이너.
 *
 * <p><b>책임:</b> ToolList의 도구 목록을 정렬하여 렌더링하고,
 * ToolRailMode의 상태(EXPAND/COLLAPSE/HIDE)에 따라 가시성을 전환한다. 모바일 여부는
 * 직교하는 {@code [mobile]} 속성으로 관리되며, 모바일 + EXPAND 조합이 드릴인 상태가 된다.
 * drill-back UI 는 {@link CloseToolRailButton} 으로 제공한다 — 데스크톱 COLLAPSE 또는
 * 모바일 EXPAND 일 때 노출된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ToolList} — 도구 목록 구독</li>
 *   <li>{@link ToolRailMode} — 레일 가시성 구독</li>
 *   <li>{@link ViewportObserver} — 모바일/데스크톱 뷰포트 구독</li>
 *   <li>{@link MenuHoverElementProvider} — 호버 위치 기반 오프셋 계산</li>
 *   <li>{@link ToolRailItemFactory} — 도구 아이템 생성</li>
 *   <li>{@link CloseToolRailButton} — drill-back 버튼</li>
 * </ul></p>
 */
@Singleton
public class ToolRailElement implements NavigationRailElement<ToolRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail");
    private final ToolRailItemFactory factory;
    private final List<ToolRailItemElement> children = new LinkedList<>();
    private final CloseToolRailButton close;
    private final MenuHoverElementProvider parent;
    private final ToolRailMode mode;
    private boolean mobile;
    @Inject ToolRailElement(ToolList list, ToolRailMode mode, MenuHoverElementProvider parent,
                            ToolRailItemFactory factory, CloseToolRailButton close, ViewportObserver viewport) {
        this.factory = factory;
        this.close = close;
        this.parent = parent;
        this.mode = mode;
        // 초기 가시성은 HIDE. [mobile] 을 mode 구독보다 먼저 동기적으로 부여하지 않으면
        // BehaviorSubject 의 즉시 emit 으로 한 프레임 desktop 레이아웃이 노출되어 flash.
        element().setAttribute("hide", true);
        this.mobile = viewport.isMobileNow();
        if (this.mobile) element().setAttribute("mobile", true);
        viewport.isMobile().subscribe(this::setMobile);
        // debounceTime 없이 즉시 구독. 이전에는 debounce(10ms) 때문에 mode 가 먼저 EXPAND 로
        // 전이해 close 버튼만 먼저 붙고 10ms 뒤 도구 아이템이 채워지는 동안 close 버튼만 홀로
        // 슬라이드업되는 flash 가 있었다.
        list.distinctUntilChanged().subscribe(this::update);
        mode.distinctUntilChanged().subscribe(this::mode);
    }
    private void update(List<Tool> tools) {
        clear();
        if(tools == null || tools.size() <= 1) return;
        tools.stream().sorted(nullsLast(comparing(Tool::order))).map(this::createItem).forEach(this::add);
        offset(parent.getValue());
        refreshCloseButton();
    }
    private ToolRailItemElement createItem(Tool tool) {
        var child = factory.item(tool);
        children.add(child);
        return child;
    }
    private void clear() {
        for(var child : children) child.element().remove();
        children.clear();
        close.element().remove();
    }
    private void mode(ToolRailState state) {
        switch (state) {
            case EXPAND -> expand();
            case COLLAPSE -> collapse();
            case HIDE -> hide();
        }
        refreshCloseButton();
    }
    private void setMobile(boolean m) {
        this.mobile = m;
        if (m) element().setAttribute("mobile", true);
        else element().removeAttribute("mobile");
        refreshCloseButton();
    }
    /**
     * drill-back close 버튼을 현재 상태에 맞게 노출/제거한다.
     * 데스크톱 COLLAPSE: 마지막 아이템으로 노출.
     * 모바일 EXPAND(드릴인): 첫 아이템으로 prepend (← 아이콘으로 드릴백 유도).
     */
    private void refreshCloseButton() {
        var state = mode.getValue();
        boolean mobileDrillIn = mobile && state == ToolRailState.EXPAND;
        boolean desktopCollapse = !mobile && state == ToolRailState.COLLAPSE;
        close.element().remove();
        if (!mobileDrillIn && !desktopCollapse) return;
        if (mobileDrillIn) {
            if (element().firstElementChild != null) element().insertBefore(close.element(), element().firstElementChild);
            else element().appendChild(close.element());
        } else {
            element().appendChild(close.element());
        }
    }
    private void offset(MenuRailItemElement parent) {
        if(parent == null) return;
        if(mobile) return; // 모바일 하단 바는 수직 오프셋 불필요
        var delta = parent.element().offsetTop - ((HTMLElement) parent.element().parentElement).offsetTop;
        var height = children.stream().mapToInt(i -> i.element().offsetHeight).sum();
        var bottom = element().clientHeight;
        if(height + delta > bottom) delta = bottom - height;
        element().style.paddingTop = CSSProperties.PaddingTopUnionType.of(delta + "px");
    }
}
