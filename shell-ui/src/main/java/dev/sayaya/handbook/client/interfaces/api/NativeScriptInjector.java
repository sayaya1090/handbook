package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.usecase.ScriptInjector;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.script;

@Singleton
public class NativeScriptInjector implements ScriptInjector {
    @Inject
    public NativeScriptInjector() {}

    @Override
    public void inject(String src) {
        if(src == null || src.isEmpty()) return;
        var existingScript = document.getElementById("module-script");
        if(existingScript != null) {
            String currentSrc = existingScript.getAttribute("src");
            if (src.equals(currentSrc)) {
                // 동일한 스크립트가 이미 로드되어 있다면 재실행을 방지하여 무한 루프나 다중 인스턴스를 막습니다.
                return;
            }
            existingScript.remove();
        }
        var scriptEl = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
        scriptEl.element().src = src;
        document.head.append(scriptEl.element());
    }
}
