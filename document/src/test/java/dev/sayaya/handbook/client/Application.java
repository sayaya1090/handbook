package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.DocumentValue;
import jsinterop.base.JsPropertyMap;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        console.log("LOG_DOCUMENT_TEST_START");
        testDocumentValue();
        testDocumentLogic();
        console.log("DOCUMENT_TEST_READY");
    }

    private void testDocumentValue() {
        DocumentValue doc = DocumentValue.create("doc-1", "type-A");
        doc.data(JsPropertyMap.of());
        doc.data().set("title", "Hello Document");
        
        console.log("LOG_DOC_DATA:" + doc.id() + ":" + doc.type() + ":" + doc.data().get("title"));
    }

    private void testDocumentLogic() {
        DocumentValue doc = new DocumentValue();
        doc.expireDateTime(1000.0);
        
        console.log("LOG_DOC_EXPIRED_TRUE:" + doc.isExpired(2000.0));
        console.log("LOG_DOC_EXPIRED_FALSE:" + doc.isExpired(500.0));
    }
}
