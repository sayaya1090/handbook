package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.jboss.elemento.Elements.div;

@Singleton
public class MenuRailElement implements NavigationRailElement<MenuRailElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail");
    private final MenuRailItemFactory factory;
    private final List<MenuRailItemElement> children = new LinkedList<>();
    @Inject MenuRailElement(MenuList list, MenuRailMode mode, MenuRailItemFactory factory) {
        this.factory = factory;
        list.distinctUntilChanged().subscribe(this::update);
        mode.distinctUntilChanged().subscribe(this::mode);
    }
    private static final Comparator<Menu> MENU_COMPARATOR = nullsLast(comparing((Menu i) -> TRUE.equals(i.bottom)).thenComparing(i -> i.order));
    private void update(List<Menu> menu) {
        clear();
        if(menu==null) return;
        AtomicBoolean bottom = new AtomicBoolean(false);
        menu.stream() .sorted(MENU_COMPARATOR).map(item -> createItem(item, bottom)) .forEach(this::add);
    }
    private MenuRailItemElement createItem(Menu menu, AtomicBoolean bottom) {
        var child = factory.item(menu);
        if(TRUE.equals(menu.bottom) && !bottom.getAndSet(true)) child.element().style.marginTop = CSSProperties.MarginTopUnionType.of("auto");
        children.add(child);
        return child;
    }
    private void clear() {
        for(var child: children) child.element().remove();
        children.clear();
    }
    private void mode(MenuRailState state) {
        switch (state) {
            case EXPAND -> expand();
            case COLLAPSE -> collapse();
            case HIDE -> hide();
        }
    }
}
