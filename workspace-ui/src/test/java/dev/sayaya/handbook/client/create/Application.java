package dev.sayaya.handbook.client.create;

import com.google.gwt.core.client.EntryPoint;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.languagePackManager().initialize();
        body().add(components.contentElement());
    }
}
