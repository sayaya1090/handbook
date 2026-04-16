package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

import static elemental2.dom.DomGlobal.console;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        console.log("LoginTest module loaded");
        Component components = DaggerComponent.create();
        components.notifyHandler();
        components.attentionHandler();
        components.highlightHandler();
        components.progressHandler();
        var content = components.content();
        elemental2.dom.DomGlobal.document.body.appendChild(content.element());
    }
}
