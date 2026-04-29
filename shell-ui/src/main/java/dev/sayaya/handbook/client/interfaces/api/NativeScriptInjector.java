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
        if(existingScript != null) existingScript.remove();
        var scriptEl = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
        scriptEl.element().src = src;
        document.head.append(scriptEl.element());
    }
}
