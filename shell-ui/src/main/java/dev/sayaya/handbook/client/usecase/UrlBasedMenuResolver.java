package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.Observable;
import elemental2.core.JsRegExp;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL 정규식 매칭을 통해 메뉴를 자동 선택한다.
 */
@Singleton
public class UrlBasedMenuResolver {
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
        if(menu != null && !menu.isEmpty()) {
            menu.stream().filter(Objects::nonNull).forEach(this::registerRegex);
            if(lastKnownUri != null) resolve(lastKnownUri);
        }
    }
    private void registerRegex(Menu menu) {
        if(menu == null || menu.urlRegex() == null) return;
        for(var regex : menu.urlRegex()) {
            map.put(new JsRegExp(regex), menu);
        }
    }
    private void onUriChanged(String newUri) {
        if (newUri == null) return;
        elemental2.dom.DomGlobal.console.log("UrlBasedMenuResolver.onUriChanged: newUri=" + newUri);
        // 1. 전체 URL이 들어온 경우 origin 제거
        String origin = DomGlobal.window.location.origin;
        if (newUri.startsWith(origin)) newUri = newUri.substring(origin.length());
        
        // 2. 프로토콜 포함 여부 재검사 (다른 origin이거나 예외 케이스)
        if (newUri.contains("://")) {
            int pathStart = newUri.indexOf("/", newUri.indexOf("://") + 3);
            if (pathStart != -1) newUri = newUri.substring(pathStart);
        }
        
        // 3. 선행 슬래시 보장
        if (!newUri.startsWith("/")) newUri = "/" + newUri;
        elemental2.dom.DomGlobal.console.log("UrlBasedMenuResolver: normalizedUri=" + newUri);
        lastKnownUri = newUri;
        if (!map.isEmpty()) resolve(newUri);
    }
    private void resolve(String uri) {
        if(map.isEmpty() || uri == null) return;
        elemental2.dom.DomGlobal.console.log("UrlBasedMenuResolver.resolve: uri=" + uri + ", map.size=" + map.size());
        map.keySet().stream()
            .filter(regex -> regex.test(uri))
            .findFirst().map(map::get)
            .ifPresent(t -> {
                elemental2.dom.DomGlobal.console.log("UrlBasedMenuResolver: matched! menu=" + t.title());
                select.next(t);
                // 모바일에서는 drawer 가 primary nav 가 아니라 secondary 오버레이이므로
                // URL 해석 결과로도 항상 HIDE 로 남겨 bottom-nav 만 노출한다.
                drawer.next(drawer.isMobile() ? DrawerState.HIDE : DrawerState.COLLAPSE);
            });
    }
}
