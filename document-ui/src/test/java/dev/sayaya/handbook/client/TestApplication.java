package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.AttributeInfo;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.handbook.client.interfaces.table.ColumnFactory;
import jsinterop.base.JsPropertyMap;

import java.util.Arrays;
import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();

        // 테스트용 타입 설정
        TypeInfo customerType = createType("customer", "name", "age", "email");
        TypeInfo orderType = createType("order", "customer", "amount", "date");

        component.typeList().next(Arrays.asList(customerType, orderType));
        component.typeProvider().next(customerType);

        // 테스트용 문서 설정
        DocumentValue doc1 = createDoc("CUST-001", "customer", "홍길동", "30", "test@example.com");
        DocumentValue doc2 = createDoc("CUST-002", "customer", "김철수", "25", "kim@example.com");
        component.spreadsheet().init(ColumnFactory.create(customerType));

        var container = div().css("doc-container")
                .add(component.controller())
                .add(component.spreadsheet())
                .element();
        body().add(container);

        // UC-D9: 에이전트 문서 조작 핸들러 초기화
        component.agentHandler().init();
    }

    private static TypeInfo createType(String id, String... attrNames) {
        TypeInfo type = new TypeInfo();
        type.id = id;
        type.version = "1.0";
        type.attributes = new AttributeInfo[attrNames.length];
        for (int i = 0; i < attrNames.length; i++) {
            type.attributes[i] = new AttributeInfo();
            type.attributes[i].name = attrNames[i];
            type.attributes[i].type = "text";
            type.attributes[i].nullable = true;
        }
        return type;
    }

    private static DocumentValue createDoc(String serial, String type, String... values) {
        DocumentValue doc = new DocumentValue();
        doc.id = "test-" + serial;
        doc.serial = serial;
        doc.type = type;
        doc.effectDateTime = System.currentTimeMillis();
        doc.expireDateTime = System.currentTimeMillis() + 86400000;
        doc.data = JsPropertyMap.of();
        return doc;
    }
}
