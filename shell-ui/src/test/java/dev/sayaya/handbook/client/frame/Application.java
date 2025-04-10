package dev.sayaya.handbook.client.frame;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.usecase.ClientWindow;
import elemental2.dom.DomGlobal;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.updater().asObservable();
        body().add(components.container().id("container"))
                .add(div().style("position: absolute; top: 100px; z-index: 999; display: flex; align-items: center; gap: 10px; margin: 10px;")
                        .add(button("Renderer 1").id("renderer1")
                                .on(EventType.click, evt-> ClientWindow.renderer.next(components.renderer1())))
                        .add(button("Renderer 2").id("renderer2")
                                .on(EventType.click, evt-> ClientWindow.renderer.next(components.renderer2())))
                );
    }
}
