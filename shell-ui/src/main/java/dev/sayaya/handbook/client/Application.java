package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().add("Hello, World!!");
        components.uri().next(window.location.href);
        components.renderer().subscribe(s->{
            DomGlobal.console.log("renderer: "+s);
        });
    }
}
