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
 * 메뉴 목록이 로드되면 "workspaces" 메뉴를 자동으로 선택하여
 * 워크스페이스 생성/참여 화면으로 유도한다.</p>
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
        if (loaded) return;
        SessionState state = sessionStateProvider.getValue();
        List<Menu> menus = menuList.getValue();
        if (state != null && state.kind() == SessionStateKind.AUTHENTICATED && menus != null && !menus.isEmpty()) {
            menus.stream()
                 .filter(menu -> "workspaces".equalsIgnoreCase(menu.title()))
                 .findFirst()
                 .ifPresent(menu -> {
                     String currentHash = window.location.hash;
                     if ("#workspaces".equalsIgnoreCase(currentHash) || "workspaces".equalsIgnoreCase(currentHash)) {
                         // 이미 해시가 설정되어 있어 hashchange 가 안 일어날 경우를 대비해 직접 선택 트리거
                         menuSelected.next(menu);
                     } else {
                         window.location.hash = "workspaces";
                     }
                     loaded = true;
                 });
        }
    }
}
