package dev.sayaya.handbook.client.canvas;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Workspace;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override public void onModuleLoad() {
        var canvas = components.canvas();
        body().add(components.controller()).add(canvas);
        components.boxCache().initialize();
        components.workspaceProvider().next(Workspace.builder().id("").name("").build());
    }
}
