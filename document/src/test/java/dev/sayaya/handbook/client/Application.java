package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import elemental2.dom.DomGlobal;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DomGlobal.console.log("LOG_DOCUMENT_TEST_START");
        GWT.log("GWT_LOG_DOCUMENT_TEST_START");
    }
}
