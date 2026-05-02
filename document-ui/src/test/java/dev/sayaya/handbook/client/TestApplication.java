package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.interfaces.table.ColumnFactory;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Type;
import elemental2.dom.DomGlobal;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        try {
            TestComponent component = DaggerTestComponent.create();
            
            // 1. 초기화
            component.documentEventHandler().init();
            component.agentDocumentHandler().init();

            // 2. 초기 상태 설정
            Type customerType = type("customer");
            Type orderType = type("order");
            List<Type> allTypes = new ArrayList<>();
            allTypes.add(customerType);
            allTypes.add(orderType);

            component.typeList().next(allTypes);
            component.typeProvider().next(customerType);

            // 3. UI 렌더링
            var root = div().css("doc-container")
                    .add(component.controller().element())
                    .add(component.spreadsheetElement().element())
                    .add(component.pagination().element());
            body().add(root);
            body().add(component.toastContainer())
                    .add(component.confirmDialog());
            
            // 4. 스프레드시트 초기화
            component.spreadsheetElement().init(ColumnFactory.create(customerType, allTypes));
            
            // 5. 워크스페이스 ID 수신 트리거
            elemental2.dom.CustomEvent<String> wsEvt = new elemental2.dom.CustomEvent<>("handbook-workspace-context");
            wsEvt.detail = "00000000-0000-0000-0000-000000000001";
            DomGlobal.window.dispatchEvent(wsEvt);
            
            DomGlobal.console.log("DOCUMENT_TEST_READY");
        } catch (Throwable e) {
            DomGlobal.console.error("FATAL ERROR during initialization: " + e.getMessage());
            GWT_printStackTrace(e);
        }
    }

    private native void GWT_printStackTrace(Throwable e) /*-{
        console.error(e.stack);
    }-*/;

    private Type type(String name) {
        return Type.create(name, "1.0", 0, 0)
                .attributes(new Attribute[] {
                        attr("name", "text"),
                        attr("age", "number")
                });
    }

    private Attribute attr(String name, String type) {
        Attribute a = new Attribute();
        a.name(name);
        return a;
    }
}
