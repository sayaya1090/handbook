package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.HTMLLinkElement;

import static org.jboss.elemento.Elements.htmlElement;

public class CreateApplication implements EntryPoint {
    private final CreateComponent components = DaggerCreateComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/workspace.css").element());
            frame.append(components.contentElement().element());
            return true;
        });
    }
}
