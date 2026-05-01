package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.table.ColumnFactory;
import dev.sayaya.handbook.domain.AttributeValue;
import dev.sayaya.handbook.domain.TypeValue;
import elemental2.dom.DomGlobal;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication {
    public void start() {
        try {
            DomGlobal.console.log("!!! TestApplication.start START !!!");
            Component component = DaggerComponent.create();
            
            TypeValue customerType = type("customer");
            List<TypeValue> allTypes = new ArrayList<>();
            allTypes.add(customerType);

            component.typeList().next(allTypes);
            component.typeProvider().next(customerType);

            // 강제 렌더링
            var root = div().css("doc-container")
                    .add(component.controller().element())
                    .add(component.spreadsheetElement().element());
            body().add(root);
            
            component.spreadsheetElement().init(ColumnFactory.create(customerType, allTypes));
            DomGlobal.console.log("!!! TestApplication.start SUCCESS !!!");
        } catch (Throwable e) {
            DomGlobal.console.error("!!! TestApplication.start FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private TypeValue type(String name) {
        TypeValue type = TypeValue.create(name, "1.0", 0, 0);
        type.attributes = new AttributeValue[] {
            attr("name", "text"),
            attr("age", "number")
        };
        return type;
    }

    private AttributeValue attr(String name, String type) {
        AttributeValue a = new AttributeValue();
        a.name = name;
        return a;
    }
}
