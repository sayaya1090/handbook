package dev.sayaya.handbook.client.redirect;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.api.FetchMock;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.historyManager().initialize();
        components.workspaceRepository().list().subscribe(list -> {});
    }
}
