package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        component.shell().initialize();
        component.agent().initialize();
    }
}
