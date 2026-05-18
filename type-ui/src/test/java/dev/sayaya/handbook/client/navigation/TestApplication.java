package dev.sayaya.handbook.client.navigation;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.usecase.WorkspaceEvent;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();
        
        // 동적 도구 관리자 초기화
        component.typeToolManager().init();
        component.typeDataCoordinator().init();

        var container = div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(div().css("type-canvas-wrapper")
                        .add(component.canvas()));

        body().add(container);

        // 워크스페이스 ID 구독 및 API 주입
        WorkspaceEventReceiver receiver = WorkspaceEvent.receiver();
        receiver.workspaceId().subscribe(id -> {
            if (id == null || id.isEmpty()) return;
            if (component.typeRepository() instanceof TypeApi) {
                ((TypeApi) component.typeRepository()).setWorkspace(id);
            }
            if (component.layoutRepository() instanceof LayoutApi) {
                ((LayoutApi) component.layoutRepository()).setWorkspace(id);
            }
            initializeData(component);
        });
    }

    private void initializeData(TestComponent component) {
        new LoadAction(
                component.typeRepository(),
                component.layoutRepository(),
                component.typeList(),
                component.positionMap(),
                null, // tracker (not needed for this test)
                component.actionManager(),
                component.layoutProvider(),
                component.layoutList(),
                component.typeDataCoordinator()
        ).execute();
    }
}
