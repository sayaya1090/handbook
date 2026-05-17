package dev.sayaya.handbook.client.navigation;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.interfaces.api.LayoutApi;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();
        
        // 워크스페이스 ID 설정 (테스트용)
        if (component.typeRepository() instanceof TypeApi) {
            ((TypeApi) component.typeRepository()).setWorkspace("demo");
        }
        if (component.layoutRepository() instanceof LayoutApi) {
            ((LayoutApi) component.layoutRepository()).setWorkspace("demo");
        }

        // 쉘 없이 독립 실행 시뮬레이션
        component.typeToolManager().init();
        component.typeDataCoordinator().init();

        var container = div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(div().css("type-canvas-wrapper")
                        .add(component.canvas()));

        body().add(container);

        // 초기 데이터 로드 실행 (현재 버그가 있는 로직)
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
