package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

public class CreateApplication implements EntryPoint {
    private final CreateComponent components = DaggerCreateComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            DomGlobal.console.log("CreateApplication");
            frame.append(components.contentElement().element());
            return true;
        });
    }
}
