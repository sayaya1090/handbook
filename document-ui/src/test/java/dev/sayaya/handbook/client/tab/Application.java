package dev.sayaya.handbook.client.tab;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Workspace;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override public void onModuleLoad() {
        components.workspaceProvider().next(Workspace.builder().id("").name("").build());
        body().add(components.tabs());
        body().add(components.controller());
        body().add(components.table());
        components.typeRepository().list().subscribe(components.typeList()::next);
    }
}
