package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().add(components.scriptElement())
              .add(components.progressElement())
              .add(components.contentElement());
    }
}
