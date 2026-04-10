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
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();

        // 테스트용 타입 설정 (다양한 속성 타입 포함)
        TypeInfo customerType = createMixedType("customer");
        TypeInfo orderType = createType("order", "customer", "amount", "date");

        List<TypeInfo> allTypes = Arrays.asList(customerType, orderType);
        component.typeList().next(allTypes);
        component.typeProvider().next(customerType);

        // 페이지네이션 컨트롤 (테스트용)
        var prevBtn = button("◀").css("doc-page-btn", "doc-page-prev");
        var nextBtn = button("▶").css("doc-page-btn", "doc-page-next");
        var pagination = div().css("doc-pagination")
                .add(prevBtn)
                .add(span().css("doc-page-info"))
                .add(nextBtn);

        var container = div().css("doc-container")
                .add(component.controller())
                .add(component.spreadsheet())
                .add(pagination)
                .element();
        body().add(container);

        // Handsontable init은 DOM에 요소가 추가된 후 호출해야 헤더가 렌더링됨
        component.spreadsheet().init(ColumnFactory.create(customerType, allTypes));

        // UC-D9: 에이전트 문서 조작 핸들러 초기화
        component.agentHandler().init();
    }

    /** 다양한 속성 타입을 포함하는 테스트 타입 생성 */
    private static TypeInfo createMixedType(String id) {
        TypeInfo type = new TypeInfo();
        type.id = id;
        type.version = "1.0";
        type.attributes = new AttributeInfo[] {
            attr("name", "text"),
            attr("age", "number"),
            attr("birthday", "date"),
            attrEnum("status", new String[]{"ACTIVE", "INACTIVE", "PENDING"}),
            attr("verified", "bool"),
            attrDoc("refOrder", "order"),
        };
        return type;
    }

    private static TypeInfo createType(String id, String... attrNames) {
        TypeInfo type = new TypeInfo();
        type.id = id;
        type.version = "1.0";
        type.attributes = new AttributeInfo[attrNames.length];
        for (int i = 0; i < attrNames.length; i++) {
            type.attributes[i] = attr(attrNames[i], "text");
        }
        return type;
    }

    private static AttributeInfo attr(String name, String type) {
        AttributeInfo a = new AttributeInfo();
        a.name = name;
        a.type = type;
        a.nullable = true;
        return a;
    }

    private static AttributeInfo attrEnum(String name, String[] values) {
        AttributeInfo a = attr(name, "enum");
        a.allowedValues = values;
        return a;
    }

    private static AttributeInfo attrDoc(String name, String refType) {
        AttributeInfo a = attr(name, "document");
        a.referencedType = refType;
        return a;
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
