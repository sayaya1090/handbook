package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.ui.elements.SelectElementBuilder.OutlinedSelectElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.ui.elements.MenuElementBuilder.Position.Popover;
import static dev.sayaya.ui.elements.SelectElementBuilder.select;

@Singleton
public class WorkspaceSelectElement implements IsElement<HTMLElement> {
    @Delegate  private final OutlinedSelectElementBuilder _this = select().outlined().label("Workspace").required(true).menuPositioning(Popover);
    private List<Workspace> workspaces;
    private Workspace selectedWorkspace;
    private final Observer<Workspace> observer;
    @Inject WorkspaceSelectElement(WorkspaceList workspaces, Observable<Workspace> observable, Observer<Workspace> observer, MenuRailMode menu) {
        this.observer = observer;
        workspaces.subscribe(list->update(list, selectedWorkspace));
        observable.distinctUntilChanged().subscribe(workspace->update(this.workspaces, workspace));
        onChange(evt->onSelect(value()));
        menu.subscribe(this::update);
    }
    private void update(List<Workspace> workspaces, Workspace selectedWorkspace) {
        this.workspaces = workspaces;
        this.selectedWorkspace = selectedWorkspace;
        removeAllOptions();
        disable(workspaces==null || workspaces.isEmpty());
        if(workspaces!=null) for(var workspace: workspaces) {
            var opt = option().value(workspace.id()).headline(workspace.name());
            if(selectedWorkspace!=null && selectedWorkspace.id().equals(workspace.id())) opt.select();
        }
    }
    private void update(MenuRailState menu) {
        if(menu == MenuRailState.HIDE || menu == MenuRailState.COLLAPSE) mode(Mode.HIDE);
        else mode(Mode.EXPAND);
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
        element().style.marginLeft = CSSProperties.MarginLeftUnionType.of(mode.marginLeft);
        element().style.width = CSSProperties.WidthUnionType.of(mode.width);
        element().style.opacity = CSSProperties.OpacityUnionType.of(mode.opacity);
        element().style.pointerEvents = mode == Mode.HIDE ? "none" : "auto";
    }

    private void onSelect(String id) {
        var workspace = workspaces.stream().filter(w->w.id().equals(id)).findFirst();
        observer.next(workspace.orElse(null));
    }
}
