package dev.sayaya.handbook.client.canvas;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.*;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();

        // 초기 레이아웃 기간 설정
        double now = System.currentTimeMillis();
        component.layoutProvider().next(LayoutPeriod.of(now, now + 86400000));
        TypeValue customer = TypeValue.create("customer", "1.0", now, now + 86400000);
        customer.description = "고객";
        customer.attributes = new AttributeValue[] {
            AttributeValue.of("name", 1, AttributeTypeValue.text()),
            AttributeValue.of("age", 2, AttributeTypeValue.number(null, null)),
            AttributeValue.of("email", 3, AttributeTypeValue.text())
        };

        TypeValue order = TypeValue.create("order", "1.0", now, now + 86400000);
        order.description = "주문";
        order.attributes = new AttributeValue[] {
            AttributeValue.of("customer", 1, AttributeTypeValue.document("customer")),
            AttributeValue.of("amount", 2, AttributeTypeValue.number(0.0, null))
        };

        component.positionMap().put(customer.key(), Position.of(50, 80, 260, 200));
        component.positionMap().put(order.key(), Position.of(400, 80, 260, 180));
        component.typeList().add(customer);
        component.typeList().add(order);

        // UC-T11/T12: 에이전트 mutation 핸들러 초기화 (생성자에서 구독 등록)
        component.agentMutationHandler();

        body()
            .add(div().css("type-container")
                .add(component.controller())
                .add(component.canvas())
                .add(component.attributeEditor()));
    }
}
