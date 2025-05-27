package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.Observable;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLinkElement;

import java.util.Arrays;

import static org.jboss.elemento.Elements.htmlElement;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.style.overflow = "auto";
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/typebox.css").element());
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/canvas.css").element());
            Observable.timer(1, 100).take(1).subscribe(tick-> {
                String url = DomGlobal.window.location.pathname + DomGlobal.window.location.search;
                update(url, frame);
            });
            components.periodService();
            return true;
        });
        components.tools().subscribe(tools -> Arrays.stream(tools).forEach(this::toolClickHandler));
    }
    private void update(String url, HTMLElement frame) {
        switch (url) {
            case "/", "/types?view=graph" -> {
                frame.append(components.controller().element());
                frame.append(components.canvas().element());
            }
            case "/types?view=calendar" -> {

            }
        }
    }
    private void toolClickHandler(Tool tool) {
        switch (tool.title()) {
            case "View as Graph"    -> tool.function(()-> components.uri().next("types?view=graph"));
            case "View as Calendar" -> tool.function(()-> components.uri().next("types?view=calendar"));
        }
    }
}
