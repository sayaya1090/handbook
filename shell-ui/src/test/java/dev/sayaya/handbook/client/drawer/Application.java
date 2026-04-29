package dev.sayaya.handbook.client.drawer;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.drawer.WorkspaceSelectElement;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.usecase.UriSharing;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    @Inject ShellAppBarElement shellAppBar;
    @Inject WorkspaceSelectElement workspaceSelect;
    @Inject WorkspaceList workspaceList;
    @Inject Observable<String> uri;
    @Inject Observer<String> uriObserver;
    @Inject Observer<dev.sayaya.handbook.domain.Progress> progressObserver;

    @Override
    public void onModuleLoad() {
        Component components = DaggerComponent.create();
        components.inject(this);
        
        body().add(shellAppBar);
        body().add(workspaceSelect);
        
        publishToWindow();
    }

    private void publishToWindow() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        uri.subscribe(v -> win.set("current_uri", v));
        
        win.set("test_uri_next", (Arg1<String>) (val) -> uriObserver.next(val));
        
        // 메뉴 선택 시뮬레이션
        win.set("test_menu_select", (Arg1<JsPropertyMap<Object>>) (map) -> {
            Menu menu = Js.cast(map);
            // menuSelected.next(menu); // 필요시 추가
        });
    }

    @FunctionalInterface
    @jsinterop.annotations.JsFunction
    interface Arg1<T> {
        void on(T val);
    }
}
