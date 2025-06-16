package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;

public class Logout implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.api().logout().then( n-> {
            Window.Location.assign("");
            return null;
        });
    }
}
