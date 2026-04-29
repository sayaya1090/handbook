package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 메뉴 선택 시 해당 모듈의 스크립트를 동적으로 주입한다.
 */
@Singleton
public class ModuleScriptManager {
    private final MenuSelected menu;
    private final ScriptInjector scriptInjector;

    @Inject ModuleScriptManager(MenuSelected menu, ScriptInjector scriptInjector) {
        this.menu = menu;
        this.scriptInjector = scriptInjector;
    }
    public void initialize() {
        menu.subscribe(this::update);
    }

    /**
     * 외부에서 스크립트 경로를 직접 주입하여 모듈을 로드한다.
     */
    public void load(String src) {
        scriptInjector.inject(src);
    }

    private void update(Menu menu) {
        if(menu == null) return;
        load(menu.script());
    }
}

