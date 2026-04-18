package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.WindowRenderBridge;
import dev.sayaya.handbook.usecase.WindowStateProviderBridge;
import dev.sayaya.handbook.usecase.WindowSearchProviderBridge;

import static org.jboss.elemento.Elements.div;

/**
 * Type-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 js/type/type.nocache.js를 로딩하면 실행된다.
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
        // shell FrameUpdater 에 Render 를 전달 — body 직접 append 는 body{position:fixed;inset:0}
        // + shell #content(100dvh) 뒤에 스택되어 뷰포트 밖으로 밀려나는 회귀를 유발한다.
        Render render = frame -> { frame.append(container); return true; };
        WindowRenderBridge.next(render);
    }

    /** 지정된 CSS 파일을 &lt;link&gt; 요소로 document.head에 추가한다. */
    private static void injectCss(String href) {
        var link = (elemental2.dom.HTMLLinkElement) elemental2.dom.DomGlobal.document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        elemental2.dom.DomGlobal.document.head.appendChild(link);
    }
}
