package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.rx.Observable;
import elemental2.core.JsRegExp;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class UrlBasedMenuResolver {
    private final static String BASE_URL = baseUrl();
    private final Map<JsRegExp, Menu> map = new ConcurrentHashMap<>();
    private final MenuList menu;
    private final Observable<String> uri;
    private final MenuSelected select;
    private final DrawerMode drawer;
    private String lastKnownUri = null;
    @Inject UrlBasedMenuResolver(MenuList menu, Observable<String> uri, MenuSelected select, DrawerMode drawer) {
        this.menu = menu;
        this.uri = uri;
        this.select = select;
        this.drawer = drawer;
    }
    public void initialize() {
        menu.subscribe(this::update);
        uri.subscribe(this::onUriChanged);
    }
    private void update(List<Menu> menu) {
        map.clear();
        if (menu != null && !menu.isEmpty()) {
            menu.stream().filter(Objects::nonNull).forEach(this::registerRegex);
            if (this.lastKnownUri != null) resolve(this.lastKnownUri);
        }
    }
    private void registerRegex(Menu menu) {
        if(menu==null || menu.urlRegex()==null) return;
        map.put(new JsRegExp(menu.urlRegex()), menu);
    }
    private void onUriChanged(String newUri) {
        if(newUri.startsWith(BASE_URL)) newUri = newUri.substring(BASE_URL.length());
        this.lastKnownUri = newUri;
        if (!map.isEmpty()) resolve(newUri);
    }
    private void resolve(String uri) {
        if (map.isEmpty() || uri == null) return;
        map.keySet().stream()
                .filter(regex-> regex.test(uri))
                .findFirst().map(map::get)
                .ifPresent(t->{
                    select.next(t);
                    drawer.next(DrawerState.COLLAPSE);
                });
    }
    public static String baseUrl() {
        var location = DomGlobal.window.location;
        String protocol = location.protocol; // 예: "https:"
        String hostname = location.hostname; // 예: "handbook.sayaya.cloud"
        return protocol + "//" + hostname + "/";
    }

}