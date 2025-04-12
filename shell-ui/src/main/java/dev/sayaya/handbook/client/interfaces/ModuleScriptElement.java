package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import elemental2.dom.HTMLScriptElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLElementBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.script;

/*
 * 메뉴 선택 -> 스크립트 주입 -> 툴 초기화 -> 프레임 생성
 */
@Singleton
public class ModuleScriptElement implements IsElement<HTMLScriptElement> {
    @Delegate private final HTMLElementBuilder<HTMLScriptElement> _this = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
    @Inject ModuleScriptElement(MenuSelected menu) {
        menu.subscribe(this::update);
    }
    private void update(Menu menu) {
        if(menu==null) return;
        element().src = menu.script();
    }
}
