package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.script;

/**
 * 메뉴 선택 시 해당 모듈의 스크립트를 동적으로 주입한다.
 */
@Singleton
public class ModuleScriptManager {
    private final MenuSelected menu;
    @Inject ModuleScriptManager(MenuSelected menu) {
        this.menu = menu;
    }
    public void initialize() {
        menu.subscribe(this::update);
    }
    private void update(Menu menu) {
        if(menu == null) return;
        var existingScript = document.getElementById("module-script");
        if(existingScript != null) existingScript.remove();
        var scriptEl = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
        scriptEl.element().src = menu.script();
        document.head.append(scriptEl.element());
    }
}
