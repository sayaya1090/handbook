package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.nav;

@Singleton
public class DrawerElement implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = nav();
    @Inject DrawerElement(DrawerMode mode, MenuToggleButton btnToggle, MenuRailElement navMenu, ToolRailElement navTools, WorkspaceSelectElement workspace) {
        _this.css("drawer")
                .add(div().css("header")
                        .add(workspace.css("workspace"))
                        .add(btnToggle.style("margin: 8px;"))
                ).add(div().style("display: flex; height: -webkit-fill-available;")
                        .add(navMenu).add(navTools));
        mode.subscribe(this::state);
    }
    private void state(DrawerState state) {
        switch (state) {
            case EXPAND -> {
                element().setAttribute("open", true);
                element().removeAttribute("hide");
            } case COLLAPSE -> {
                element().removeAttribute("open");
                element().removeAttribute("hide");
            } case HIDE -> {
                element().removeAttribute("open");
                element().setAttribute("hide", true);
            }
        }
    }
}
