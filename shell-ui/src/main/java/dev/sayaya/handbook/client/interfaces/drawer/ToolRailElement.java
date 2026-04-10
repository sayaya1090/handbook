package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.ToolList;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.domain.Tool;
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
 * ToolRailMode의 상태(EXPAND/COLLAPSE/HIDE/HORIZONTAL_CHIPS)에 따라 레이아웃을 전환한다.
 * 모바일(HORIZONTAL_CHIPS)에서는 수평 스크롤 가능한 칩 바로 표시된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ToolList} — 도구 목록 구독</li>
 *   <li>{@link ToolRailMode} — 레일 상태 구독</li>
 *   <li>{@link MenuHoverElementProvider} — 호버 위치 기반 오프셋 계산</li>
 *   <li>{@link ToolRailItemFactory} — 도구 아이템 생성</li>
 *   <li>{@link CloseToolRailButton} — COLLAPSE 상태의 닫기 버튼</li>
 * </ul></p>
 */
@Singleton
public class ToolRailElement implements NavigationRailElement<ToolRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail");
    private final ToolRailItemFactory factory;
    private final List<ToolRailItemElement> children = new LinkedList<>();
    private final CloseToolRailButton close;
    private final MenuHoverElementProvider parent;
    @Inject ToolRailElement(ToolList list, ToolRailMode mode, MenuHoverElementProvider parent, ToolRailItemFactory factory, CloseToolRailButton close) {
        this.factory = factory;
        this.close = close;
        this.parent = parent;
        list.distinctUntilChanged().debounceTime(10).subscribe(this::update);
        mode.distinctUntilChanged().subscribe(this::mode);
    }
    private void update(List<Tool> tools) {
        clear();
        if(tools == null || tools.size() <= 1) return;
        tools.stream().sorted(nullsLast(comparing(Tool::order))).map(this::createItem).forEach(this::add);
        offset(parent.getValue());
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
            case EXPAND -> {
                expand();
                close.element().remove();
            }
            case COLLAPSE -> {
                collapse();
                add(close);
            }
            case HIDE -> hide();
            case HORIZONTAL_CHIPS -> horizontalChips();
        }
    }

    private void horizontalChips() {
        element().removeAttribute("expand");
        element().removeAttribute("collapse");
        element().removeAttribute("hide");
        element().setAttribute("horizontal-chips", true);
        close.element().remove();
    }
    private void offset(MenuRailItemElement parent) {
        if(parent == null) return;
        var delta = parent.element().offsetTop - ((HTMLElement) parent.element().parentElement).offsetTop;
        var height = children.stream().mapToInt(i -> i.element().offsetHeight).sum();
        var bottom = element().clientHeight;
        if(height + delta > bottom) delta = bottom - height;
        element().style.paddingTop = CSSProperties.PaddingTopUnionType.of(delta + "px");
    }
}
