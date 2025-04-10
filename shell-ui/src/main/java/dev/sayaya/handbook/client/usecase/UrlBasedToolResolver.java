package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.Observable;
import elemental2.core.JsRegExp;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class UrlBasedToolResolver {
    private final Map<JsRegExp, Tool> map = new HashMap<>();
    private final MenuList menu;
    private final Observable<String> uri;
    private final ToolSelected select;
    private final DrawerMode drawer;
    @Inject UrlBasedToolResolver(MenuList menu, Observable<String> uri, ToolSelected select, DrawerMode drawer) {
        this.menu = menu;
        this.uri = uri;
        this.select = select;
        this.drawer = drawer;
    }
    public void initialize() {
        menu.subscribe(this::update);
        uri.subscribe(this::resolve);
    }
    private void update(List<Menu> menu) {
        map.clear();
        menu.stream().filter(Objects::nonNull)
                .map(Menu::tools).filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .forEach(this::registerRegex);
    }
    private void registerRegex(Tool tool) {
        if(tool==null || tool.uriRegex()==null) return;
        map.put(new JsRegExp(tool.uriRegex()), tool);
    }
    private void resolve(String uri) {
        map.keySet().stream().filter(regex->regex.test(uri))
                .findFirst().map(map::get)
                .ifPresent(t->{
                    select.next(t);
                    drawer.next(DrawerState.COLLAPSE);
                });
    }
}