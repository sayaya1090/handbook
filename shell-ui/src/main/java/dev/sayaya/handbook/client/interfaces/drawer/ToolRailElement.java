package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.client.usecase.ToolList;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
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
        if(tools ==null || tools.size() <=1) return;
        tools.stream().sorted(nullsLast(comparing(Tool::order))).map(this::createItem).forEach(this::add);
        offset(parent.getValue());
    }
    private ToolRailItemElement createItem(Tool tool) {
        var child = factory.item(tool);
        children.add(child);
        return child;
    }
    private void clear() {
        for(var child: children) child.element().remove();
        children.clear();
        close.element().remove();
    }
    private void mode(ToolRailState state) {
        switch (state) {
            case EXPAND -> {
                expand();
                close.element().remove();
            } case COLLAPSE -> {
                collapse();
                add(close);
            } case HIDE -> hide();
        }
    }
    private void offset(MenuRailItemElement parent) {
        if(parent==null) return;
        var delta = parent.element().offsetTop - ((HTMLElement)parent.element().parentElement).offsetTop;
        var height = children.stream().mapToInt(i -> i.element().offsetHeight).sum();
        var bottom = element().clientHeight;
        if(height + delta > bottom) delta = bottom - height;
        element().style.paddingTop = CSSProperties.PaddingTopUnionType.of(delta + "px");
    }
}
