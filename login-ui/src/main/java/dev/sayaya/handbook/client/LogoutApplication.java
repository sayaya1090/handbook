package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

public class LogoutApplication implements EntryPoint {
    private Component components;
    @Override
    public void onModuleLoad() {
        components = DaggerComponent.create();
        components.api().logout().then(v -> {
            DomGlobal.window.location.assign("");
            return null;
        });
    }
}
