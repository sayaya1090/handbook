package dev.sayaya.handbook.client.history;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Component;
import static elemental2.dom.DomGlobal.window;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class TestApplication implements EntryPoint {
    @Inject HistoryManager historyManager;
    @Inject BehaviorSubject<String> uri;
    @Inject MenuSelected menuSelected;

    @Override
    public void onModuleLoad() {
        DaggerTestApplication_TestComponent.create().inject(this);
        historyManager.initialize();
        publishToWindow();
    }

    private void publishToWindow() {
        JsPropertyMap<Object> win = Js.asPropertyMap(window);
        // Java -> JS: URI 스트림의 현재 값을 문자열로 노출
        uri.subscribe(v -> win.set("current_uri", v));
        
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

    @Singleton
    @Component(modules = { dev.sayaya.handbook.client.HostSharedModule.class, dev.sayaya.handbook.client.Module.class })
    interface TestComponent {
        void inject(TestApplication app);
    }
}
