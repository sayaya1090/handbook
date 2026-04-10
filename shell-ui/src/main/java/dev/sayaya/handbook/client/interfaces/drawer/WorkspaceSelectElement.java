package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.ui.elements.SelectElementBuilder.OutlinedSelectElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;

@Singleton
public class WorkspaceSelectElement implements IsElement<HTMLElement> {
    private OutlinedSelectElementBuilder _this = select().outlined().label("Workspace").required(true).menuPositioning("popover");
    private List<Workspace> workspaces;
    @Inject WorkspaceSelectElement(WorkspaceList workspaces, MenuRailMode menu) {
        workspaces.subscribe(this::update);
        menu.subscribe(this::update);
    }
    private void update(List<Workspace> workspaces) {
        this.workspaces = workspaces;
        if(workspaces == null || workspaces.isEmpty()) {
            _this.disabled(true);
            return;
        }
        _this.removeAllOptions();
        _this.disabled(false);
        for (var workspace : workspaces) _this.option().value(workspace.id()).headline(workspace.name());
    }
    private void update(MenuRailState menu) {
        if(menu == MenuRailState.HIDE || menu == MenuRailState.COLLAPSE) mode(Mode.HIDE);
        else mode(Mode.EXPAND);
    }
    private String css;
    public WorkspaceSelectElement css(String css) {
        this.css = css;
        _this.css(css);
        return this;
    }
    @Override
    public HTMLElement element() {
        return _this.element();
    }
    private enum Mode {
        HIDE("0rem", "0rem", 0),
        EXPAND("1rem", "100%", 1);

        private final String marginLeft;
        private final String width;
        private final double opacity;

        Mode(String marginLeft, String width, double opacity) {
            this.marginLeft = marginLeft;
            this.width = width;
            this.opacity = opacity;
        }
    }
    private void mode(Mode mode) {
        _this.element().style.marginLeft = CSSProperties.MarginLeftUnionType.of(mode.marginLeft);
        _this.element().style.width = CSSProperties.WidthUnionType.of(mode.width);
        _this.element().style.opacity = CSSProperties.OpacityUnionType.of(mode.opacity);
        _this.element().style.pointerEvents = mode == Mode.HIDE ? "none" : "auto";
    }
}
