package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.SessionState;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.SessionStateKind;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.core.JsArray;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static elemental2.dom.DomGlobal.window;

/**
 * 온보딩 부트스트래퍼.
 * <p>사용자가 인증되었으나 워크스페이스가 없는 상태(AUTHENTICATED)에서,
 * 워크스페이스 관리 기능이 있는 메뉴가 감지되면 해당 화면으로 자동 유도한다.</p>
 */
@Singleton
public class WorkspaceOnboardingBootstrapper {
    private final SessionStateProvider sessionStateProvider;
    private final WorkspaceList workspaceList;
    private final MenuList menuList;
    private final MenuSelected menuSelected;
    private final BehaviorSubject<String> uri;
    private boolean loaded = false;

    @Inject
    public WorkspaceOnboardingBootstrapper(SessionStateProvider sessionStateProvider,
                                         WorkspaceList workspaceList,
                                         MenuList menuList,
                                         MenuSelected menuSelected,
                                         BehaviorSubject<String> uri) {
        this.sessionStateProvider = sessionStateProvider;
        this.workspaceList = workspaceList;
        this.menuList = menuList;
        this.menuSelected = menuSelected;
        this.uri = uri;
    }

    public void initialize() {
        sessionStateProvider.subscribe(state -> recompute());
        workspaceList.subscribe(workspaces -> recompute());
        menuList.subscribe(menus -> recompute());
    }

    private void recompute() {
        SessionState state = sessionStateProvider.getValue();
        if (state == null || state.kind() != SessionStateKind.AUTHENTICATED) {
            loaded = false;
        }
        if (loaded) return;
        
        List<Menu> menus = menuList.getValue();
        if (state != null && state.kind() == SessionStateKind.AUTHENTICATED && menus != null && !menus.isEmpty()) {
            menus.stream()
                 .filter(menu -> {
                     if (menu.title() != null && menu.title().toLowerCase().contains("workspace")) return true;
                     JsPropertyMap<Object> map = Js.cast(menu);
                     Object raw = map.get("url_regex");
                     if (raw instanceof JsArray) {
                         JsArray<String> regexes = Js.cast(raw);
                         for (int i = 0; i < regexes.length; i++) {
                             String regex = regexes.getAt(i);
                             if (regex != null && regex.contains("/workspaces")) return true;
                         }
                     }
                     return false;
                 })
                 .findFirst()
                 .ifPresent(menu -> {
                     // 해시 대신 시스템 공식 URI 스트림을 사용하여 클린 URL 네비게이션 수행
                     uri.next("/workspaces");
                     loaded = true;
                 });
        }
    }
}
