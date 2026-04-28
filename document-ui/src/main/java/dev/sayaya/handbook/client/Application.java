package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.usecase.WindowRenderBridge;
import dev.sayaya.handbook.usecase.WindowSearchProviderBridge;
import dev.sayaya.handbook.usecase.WindowStateProviderBridge;
import dev.sayaya.handbook.usecase.WindowWorkspaceEventBridge;
import elemental2.dom.DomGlobal;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DomGlobal.console.log("!!! Document-UI onModuleLoad START !!!");
        try {
            Component component = DaggerComponent.create();
            
            // 필수 컴포넌트 체크
            if (component.spreadsheetElement() == null || component.controller() == null) {
                throw new IllegalStateException("Dagger component initialization incomplete");
            }

            component.documentEventHandler().init();
            body().add(component.toastContainer());

            component.typeRepository().list(null).subscribe(types -> {
                if (types != null && !types.isEmpty()) {
                    List<TypeValue> typeList = new ArrayList<>(types);
                    component.typeList().next(typeList);
                    component.typeProvider().next(typeList.get(0));
                }
            });

            Render render = frame -> {
                frame.innerHTML = "";
                frame.append(component.spreadsheetElement().element());
                return true;
            };
            WindowRenderBridge.next(render);

            WindowStateProviderBridge.register(component.documentStateProvider());
            WindowSearchProviderBridge.register(query -> "{\"results\":[]}");
            DomGlobal.console.log("!!! Document-UI onModuleLoad SUCCESS !!!");
        } catch (Throwable e) {
            DomGlobal.console.error("!!! FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
