package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.handbook.client.usecase.WorkspaceProvider;
import dev.sayaya.ui.elements.SelectElementBuilder.OutlinedSelectElementBuilder;
import elemental2.dom.DomGlobal;
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
    @Inject WorkspaceSelectElement(WorkspaceList workspaces, WorkspaceProvider provider, DrawerMode mode) {
        workspaces.subscribe(this::update);
        onChange(evt->onSelect(value()));
        mode.subscribe(this::update);
    }
    private void update(List<Workspace> workspaces) {
        removeAllOptions();
        disable(workspaces==null || workspaces.isEmpty());
        if(workspaces!=null) for(var workspace: workspaces) option().value(workspace.id()).headline(workspace.name());
    }
    private void update(DrawerState mode) {
        switch (mode) {
            case COLLAPSE, HIDE -> hidden(true);
            case EXPAND -> hidden(false);
        }
    }
    private void onSelect(String id) {
        DomGlobal.console.log(id);
    }
}
