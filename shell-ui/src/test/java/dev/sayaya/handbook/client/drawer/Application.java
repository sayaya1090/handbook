package dev.sayaya.handbook.client.drawer;

import com.google.gwt.core.client.EntryPoint;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().style("display: flex; height: -webkit-fill-available; inset: 0;").add(components.drawer());
    }
}
