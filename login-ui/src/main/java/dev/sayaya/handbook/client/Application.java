package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.interfaces.ui.ContentElement;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.HTMLScriptElement;

import static elemental2.dom.DomGlobal.document;

public class Application implements EntryPoint {
    private Component components;
    @Override
    public void onModuleLoad() {
        components = DaggerComponent.create();
        Observer<Render> renderer = components.renderer();
        ContentElement content = components.content();
        renderer.next(frame -> {
            loadCss("css/console.css");
            loadCss("css/login.css");
            loadCss("css/brands.min.css");
            loadScript("js/brands.min.js");
            frame.append(content.element());
            return true;
        });
    }
    private void loadCss(String href) {
        HTMLLinkElement link = (HTMLLinkElement) document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        document.head.appendChild(link);
    }
    private void loadScript(String src) {
        HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
        script.src = src;
        document.head.appendChild(script);
    }
}
