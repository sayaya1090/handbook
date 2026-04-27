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

    /**
     * 외부에서 스크립트 경로를 직접 주입하여 모듈을 로드한다.
     */
    public void load(String src) {
        if(src == null || src.isEmpty()) return;
        var existingScript = document.getElementById("module-script");
        if(existingScript != null) existingScript.remove();
        var scriptEl = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
        scriptEl.element().src = src;
        document.head.append(scriptEl.element());
    }

    private void update(Menu menu) {
        if(menu == null) return;
        load(menu.script());
    }
}
