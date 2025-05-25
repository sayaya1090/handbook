package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.rx.Observable;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLinkElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlElement;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.style.overflow = "auto";
            Observable.timer(1, 100).take(1).subscribe(tick-> {
                String url = DomGlobal.window.location.pathname + DomGlobal.window.location.search;
                update(url, frame);
                components.typeApi().initialize();
                components.documentApi();
            });
            return true;
        });
    }
    private void update(String url, HTMLElement frame) {
        switch (url) {
            case "/", "/types?view=graph" -> {
                frame.append(htmlElement("script", HTMLLinkElement.class).attr("src", "js/handsontable.full.min.js").element());
                frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/handsontable.full.min.css").element());
                frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/document.css").element());
                frame.append(components.tabs().element());
                frame.append(components.controller().element());
                frame.append(div().style("padding: 1rem;")
                        .add(div().style("""
                        overflow: hidden;
                        height: calc(100dvh - 2rem);
                        height: fit-content;
                        """).add(components.table())
                ).element());
            }
            case "/types?view=calendar" -> {

            }
        }
    }
}
