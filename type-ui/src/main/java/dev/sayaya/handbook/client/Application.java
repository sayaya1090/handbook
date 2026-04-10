package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.usecase.WindowStateProviderBridge;
import dev.sayaya.handbook.usecase.WindowSearchProviderBridge;

import static org.jboss.elemento.Elements.div;

/**
 * Type-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 type/type.nocache.js를 로딩하면 실행된다.
 * Canvas + Controller를 Render로 shell 프레임에 전달한다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        injectCss("css/type-ui.css");

        // 워크스페이스 이벤트 핸들러 초기화 (실시간 협업)
        component.typeEventHandler().init();
        org.jboss.elemento.Elements.body().add(component.toastContainer());

        // 에이전트 브릿지 등록: StateProvider, SearchProvider
        WindowStateProviderBridge.register(component.typeStateProvider());
        WindowSearchProviderBridge.register(q -> {
            // TypeSearchProvider.search()는 BehaviorSubject를 반환하므로 동기적으로 값을 꺼낸다
            final String[] result = {null};
            component.typeSearchProvider().search(q).subscribe(v -> result[0] = v);
            return result[0];
        });

        var container = div().css("type-container")
                .add(component.controller())
                .add(component.canvas())
                .add(component.attributeEditor())
                .element();
        org.jboss.elemento.Elements.body().add(container);
    }

    private static native void injectCss(String href) /*-{
        var link = $doc.createElement('link');
        link.rel = 'stylesheet';
        link.href = href;
        $doc.head.appendChild(link);
    }-*/;
}
