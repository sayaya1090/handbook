package dev.sayaya.handbook.client.usecase;

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
        
        // HomeRedirector 초기화 (참여 중인 워크스페이스가 있을 때 /dashboard 로 이동)
        new HomeRedirector(components.uriStore(), components.workspaceList(), components.uriObserver()).initialize();

        components.workspaceRepository().list().subscribe(list -> {});
    }
}
