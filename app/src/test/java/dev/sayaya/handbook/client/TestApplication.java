package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();
        component.shell().initialize();
        component.agent().initialize();
    }
}
