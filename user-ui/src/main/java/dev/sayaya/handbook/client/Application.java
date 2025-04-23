package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.append("User");
            return true;
        });
    }
}
