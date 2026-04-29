package dev.sayaya.handbook.client.history;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.domain.Menu;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import static elemental2.dom.DomGlobal.window;

public class TestApplication implements EntryPoint {
    private HistoryManager historyManager;
    private UriStore uri;
    private MenuSelected menuSelected;

    @Override
    public void onModuleLoad() {
        var components = DaggerTestComponent.create();
        this.historyManager = components.historyManager();
        this.uri = components.uri();
        this.menuSelected = components.menuSelected();

        historyManager.initialize();
        publishToWindow();
    }

    private void publishToWindow() {
        JsPropertyMap<Object> win = Js.asPropertyMap(window);
        // Java -> JS: URI 스트림의 현재 값을 문자열로 노출

        uri.subscribe(v -> win.set("current_uri", v));
        menuSelected.subscribe(m -> {
            if (m == null) win.set("selected_menu", null);
            else win.set("selected_menu", m.title());
        });

        // JS -> Java: URI 스트림에 값을 입력하는 브릿지 함수
        win.set("test_uri_next", (Arg1<String>) (val) -> uri.next(val));
        
        // JS -> Java: 메뉴 선택을 시뮬레이션하는 브릿지 함수
        win.set("test_menu_select", (Arg1<JsPropertyMap<Object>>) (map) -> {
            Menu menu = Js.cast(map);
            menuSelected.next(menu);
        });
        }
    @FunctionalInterface
    @jsinterop.annotations.JsFunction
    interface Arg1<T> {
        void on(T val);
    }
}
