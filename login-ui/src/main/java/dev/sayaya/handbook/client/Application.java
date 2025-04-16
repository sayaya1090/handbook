package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.HTMLLinkElement;

import static org.jboss.elemento.Elements.htmlElement;
import static org.jboss.elemento.Elements.script;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/console.css").element());
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/login.css").element());
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/brands.min.css").element());
            var brands = script().attr("type", "text/javascript").attr("async", "true");
            brands.attr("src", "js/brands.min.js");
            frame.append(brands.element());
            frame.append(components.content().element());
           return true;
        });
    }
}
