package dev.sayaya.handbook.client.canvas;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.*;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();

        // 2026-05-06: 레이아웃 자동 선택 로직 검증을 위해 두 개의 레이아웃 주입
        double now = 1778025600000.0; 
        double past = now - 86400000.0; 
        double infinity = 253402214400000.0;
        
        TypeLayout currentLayout = TypeLayout.create("l1", "demo", now, infinity, null);
        TypeLayout pastLayout = TypeLayout.create("l0", "demo", past, now, null);
        
        // LayoutList 에 두 레이아웃 등록
        component.layoutList().replace(java.util.List.of(pastLayout, currentLayout));
        // LayoutProvider 에 선택 요청 (자동 선택 로직 발화)
        component.layoutProvider().selectBestMatch(component.layoutList().getValue());

        Type customer = Type.create("customer", "1.0", past, infinity);
        customer.description("고객");
        customer.attributes(new Attribute[] {
            Attribute.create(null, "name", 1, AttributeType.text()),
            Attribute.create(null, "age", 2, AttributeType.number(null, null)),
            Attribute.create(null, "email", 3, AttributeType.text())
        });

        Type order = Type.create("order", "1.0", past, infinity);
        order.description("주문");
        order.attributes(new Attribute[] {
            Attribute.create(null, "customer", 1, AttributeType.document("customer")),
            Attribute.create(null, "amount", 2, AttributeType.number(0.0, null))
        });

        component.positionMap().put(customer.key(), Position.of(50, 80, 260, 200));
        component.positionMap().put(order.key(), Position.of(400, 80, 260, 180));
        component.typeList().add(customer);
        component.typeList().add(order);

        // UC-T11/T12: 에이전트 mutation 핸들러 초기화 (생성자에서 구독 등록)
        component.agentMutationHandler();
        component.periodRecalculationService();

        // 테스트를 위해 ValidatorEditorFactory 를 전역 노출
        exportTestApi(component);

        body()
            .add(div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(div().css("type-canvas-wrapper")
                        .add(component.typeInspectorPanel())
                        .add(component.typeFloatingToolbar())
                        .add(component.typeBottomSheet())
                        .add(component.canvas()))
                .add(component.attributeEditor())
                .add(component.dateCorrectionDialog())
                .add(component.versionCreationDialog())
                .add(component.conflictResolutionDialog())
                .add(component.actionDial())
                .add(component.settingsDial()));
    }

    private void exportTestApi(TestComponent component) {
        TestApi api = type -> component.validatorEditorFactory().create(type) != null;
        jsinterop.base.Js.asPropertyMap(elemental2.dom.DomGlobal.window).set("testValidatorFactory", api);
    }

    @FunctionalInterface
    @jsinterop.annotations.JsType
    public interface TestApi {
        boolean exists(String type);
    }
}
