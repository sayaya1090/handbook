package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;
import elemental2.dom.DomGlobal;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        testLayoutPeriod();
        testAttributeType();
        testTypeValue();
        console.log("SCHEMA_TEST_READY");
    }

    private void testLayoutPeriod() {
        LayoutPeriod p1 = LayoutPeriod.of(100, 200);
        LayoutPeriod p2 = LayoutPeriod.of(150, 250);
        console.log("LOG_OVERLAP_RESULT:" + p1.overlap(p2));
    }

    private void testAttributeType() {
        AttributeType atv = AttributeType.array(AttributeType.text());
        console.log("LOG_SIMPLIFY_RESULT:" + atv.simplify());
    }

    private void testTypeValue() {
        Type type = Type.create("t-1", "1.0", 500, 400);
        console.log("LOG_TYPE_RESULT:" + type.id() + ":" + type.version() + ":" + type.width());
    }
}
