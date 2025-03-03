package dev.sayaya.handbook.client.box;

import com.google.gwt.core.client.EntryPoint;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        body().add("Hello");
    }
}
