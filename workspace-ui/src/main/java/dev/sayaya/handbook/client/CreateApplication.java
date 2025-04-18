package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

public class CreateApplication implements EntryPoint {
    private final CreateComponent components = DaggerCreateComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.append(components.contentElement().element());
            return true;
        });
    }
}
