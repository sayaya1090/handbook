package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.SessionState;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.SessionStateKind;

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
    private final MenuList menuList;
    private final MenuSelected menuSelected;
    private boolean loaded = false;

    @Inject
    public WorkspaceOnboardingBootstrapper(SessionStateProvider sessionStateProvider,
                                         MenuList menuList,
                                         MenuSelected menuSelected) {
        this.sessionStateProvider = sessionStateProvider;
        this.menuList = menuList;
        this.menuSelected = menuSelected;
    }

    public void initialize() {
        sessionStateProvider.subscribe(state -> recompute());
        menuList.subscribe(menus -> recompute());
    }

    private void recompute() {
        SessionState state = sessionStateProvider.getValue();
        // [Issue 3] AUTHENTICATED 가 아닌 상태가 되면 loaded 플래그를 리셋하여 SPA 상태 누수 방지
        if (state == null || state.kind() != SessionStateKind.AUTHENTICATED) {
            loaded = false;
        }
        if (loaded) return;
        
        List<Menu> menus = menuList.getValue();
        if (state != null && state.kind() == SessionStateKind.AUTHENTICATED && menus != null && !menus.isEmpty()) {
            menus.stream()
                 .filter(menu -> {
                     // [Issue 1] 제목 매칭 대신 URL 패턴 기반으로 검색 (다국어/계층 대응)
                     if (menu.urlRegex() == null) return false;
                     for (String regex : menu.urlRegex()) {
                         if (regex != null && regex.contains("/workspaces")) return true;
                     }
                     return false;
                 })
                 .findFirst()
                 .ifPresent(menu -> {
                     String currentHash = window.location.hash;
                     if ("#workspaces".equalsIgnoreCase(currentHash) || "workspaces".equalsIgnoreCase(currentHash)) {
                         menuSelected.next(menu);
                     } else {
                         window.location.hash = "workspaces";
                     }
                     loaded = true;
                 });
        }
    }
}
