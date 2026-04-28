package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.AttributeTypeValue;
import dev.sayaya.handbook.domain.LayoutPeriod;
import elemental2.dom.DomGlobal;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        testLayoutPeriodOverlap();
        testAttributeTypeSimplify();
        DomGlobal.console.log("SCHEMA_DOMAIN_TEST_READY");
    }

    private void testLayoutPeriodOverlap() {
        LayoutPeriod p1 = LayoutPeriod.of(100, 200);
        LayoutPeriod p2 = LayoutPeriod.of(150, 250);
        DomGlobal.console.log("LOG_OVERLAP_RESULT:" + p1.overlap(p2)); // Expected: 50
    }

    private void testAttributeTypeSimplify() {
        AttributeTypeValue atv = AttributeTypeValue.array(AttributeTypeValue.text());
        DomGlobal.console.log("LOG_SIMPLIFY_RESULT:" + atv.simplify()); // Expected: text[]
    }
}
