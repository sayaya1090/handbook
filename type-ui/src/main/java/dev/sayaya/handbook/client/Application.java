package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().add(components.canvas());
        components.uri().subscribe(uri -> {
            DomGlobal.console.log("uri: "+uri);
        });
        components.renderer().next(elem-> {
            elem.append("Hello");
            return true;
        });
    }
}
