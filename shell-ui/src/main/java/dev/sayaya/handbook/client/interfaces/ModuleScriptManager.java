package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuSelected;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.script;

/*
 * 메뉴 선택 -> 스크립트 주입 -> 툴 초기화 -> 프레임 생성
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
        if(menu==null) return;
        var existingScript = document.getElementById("module-script");
        if (existingScript != null) existingScript.remove();
        var script = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
        script.element().src = menu.script();
        document.head.append(script.element());
    }
}
