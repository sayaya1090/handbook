package dev.sayaya.handbook.client.canvas;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();

        // 2026-05-06: 레이아웃 자동 선택 로직 검증을 위해 두 개의 기간을 주입
        double now = 1714953600000.0; // 2024-05-06
        double past = now - 86400000.0; // 어제
        LayoutPeriod currentPeriod = LayoutPeriod.of(now, Double.MAX_VALUE);
        LayoutPeriod pastPeriod = LayoutPeriod.of(past, now);
        
        // LayoutList 에 두 기간 등록
        component.layoutList().replace(java.util.List.of(pastPeriod, currentPeriod));
        
        // LayoutProvider 에 선택 요청 (자동 선택 로직 발화)
        component.layoutProvider().selectBestMatch(component.layoutList().getValue());

        Type customer = Type.create("customer", "1.0", now, Double.MAX_VALUE);
        customer.description("고객");
        customer.attributes(new Attribute[] {
            Attribute.create(null, "name", 1, AttributeType.text()),
            Attribute.create(null, "age", 2, AttributeType.number(null, null)),
            Attribute.create(null, "email", 3, AttributeType.text())
        });

        Type order = Type.create("order", "1.0", now, now + 86400000);
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

        body()
            .add(div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(component.canvas())
                .add(component.attributeEditor())
                .add(component.dateCorrectionDialog())
                .add(component.versionCreationDialog())
                .add(component.actionDial())
                .add(component.settingsDial()));
    }
}
