package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.handbook.client.interfaces.table.ColumnFactory;
import dev.sayaya.handbook.usecase.WindowStateProviderBridge;

import java.util.Arrays;
import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

/**
 * Document-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 js/data.nocache.js를 로딩하면 실행된다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        injectCss("css/document-ui.css");

        // 에이전트 브릿지 등록
        WindowStateProviderBridge.register(component.documentStateProvider());

        // 에이전트 핸들러 초기화
        component.agentHandler().init();

        // 워크스페이스 이벤트 핸들러 초기화 (실시간 협업)
        component.documentEventHandler().init();
        body().add(component.toastContainer());

        // 타입 목록 로딩 → 첫 번째 타입 선택 → 스프레드시트 초기화
        component.typeRepository().list().subscribe(types -> {
            if (types != null && types.length > 0) {
                List<TypeInfo> typeList = Arrays.asList(types);
                component.typeList().next(typeList);
                component.typeProvider().next(types[0]);
            }
        });

        // 타입 선택 변경 시 컬럼 재구성 + 문서 로딩
        component.typeProvider().asObservable().subscribe(type -> {
            if (type == null) return;
            var columns = ColumnFactory.create(type);
            component.spreadsheet().init(columns);
            component.documentApi().search(type.id, 0, 50).subscribe(docs -> {
                if (docs != null) {
                    component.spreadsheet().element(); // ensure rendered
                }
            });
        });

        var container = div().css("doc-container")
                .add(component.controller())
                .add(component.spreadsheet())
                .element();
        body().add(container);
    }

    private static native void injectCss(String href) /*-{
        var link = $doc.createElement('link');
        link.rel = 'stylesheet';
        link.href = href;
        $doc.head.appendChild(link);
    }-*/;
}
