package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.ToolRailState;
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
 * ToolRailMode의 상태(EXPAND/COLLAPSE/HIDE)에 따라 가시성을 전환한다. 모바일 여부는
 * 직교하는 {@code [mobile]} 속성으로 관리되며, 모바일 + EXPAND 조합이 드릴인 상태가 된다.
 * drill-back UI 는 {@link CloseToolRailButton} 으로 제공한다 — 데스크톱 COLLAPSE 또는
 * 모바일 EXPAND 일 때 노출된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuSelectedElementProvider} — 선택 위치 기반 오프셋 계산 (UC-S6 폐기 후 click 기반)</li>
 *   <li>{@link ToolRailItemFactory} — 도구 아이템 생성</li>
 *   <li>{@link CloseToolRailButton} — drill-back 버튼</li>
 * </ul></p>
 */
@Singleton
public class ToolRailElement implements NavigationRailElement<ToolRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail", "tool-rail");
    private final ToolRailItemFactory factory;
    private final List<ToolRailItemElement> children = new LinkedList<>();
    private final CloseToolRailButton close;
    private final MenuSelectedElementProvider parent;
    private ToolRailState currentState = ToolRailState.HIDE;
    private boolean mobile = false;

    @Inject ToolRailElement(MenuSelectedElementProvider parent,
                            ToolRailItemFactory factory, CloseToolRailButton close) {
        this.factory = factory;
        this.close = close;
        this.parent = parent;
        element().setAttribute("hide", true);
    }

    public void update(List<Tool> tools) {
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

    public void setMode(ToolRailState state) {
        this.currentState = state;
        switch (state) {
            case EXPAND -> expand();
            case COLLAPSE -> collapse();
            case HIDE -> hide();
        }
        refreshCloseButton();
    }

    public void setMobile(boolean m) {
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
        var state = currentState;
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

    public void offset(MenuRailItemElement anchor) {
        if(anchor == null || mobile) return;
        
        elemental2.dom.DomGlobal.requestAnimationFrame(t -> {
            HTMLElement anchorEl = anchor.element();
            HTMLElement railEl = element();
            HTMLElement bodyEl = (HTMLElement) railEl.parentElement;
            
            if (anchorEl == null || bodyEl == null) return;
            
            // drawer-body 기준 수직 위치 계산 (절대 좌표 차이 이용)
            double anchorTop = anchorEl.getBoundingClientRect().top;
            double bodyTop = bodyEl.getBoundingClientRect().top;
            double delta = anchorTop - bodyTop;
            
            double railHeight = children.stream().mapToDouble(i -> i.element().offsetHeight).sum();
            double bodyHeight = bodyEl.clientHeight;
            
            if(bodyHeight > 0) {
                // 하단 경계 초과 방지
                if(delta + railHeight > bodyHeight) delta = bodyHeight - railHeight;
                if(delta < 0) delta = 0;
                
                railEl.style.paddingTop = elemental2.dom.CSSProperties.PaddingTopUnionType.of(delta + "px");
                // 디버깅용 로그 (나중에 제거 가능)
                elemental2.dom.DomGlobal.console.log("ToolRail offset alignment: " + delta + "px at anchor " + anchorEl.dataset.get("menuTitle"));
            }
        });
    }
}

