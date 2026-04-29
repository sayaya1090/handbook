package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import dev.sayaya.handbook.domain.Workspace;

/**
 * 사용자가 로그인했을 때 워크스페이스 컨텍스트로 자동 안내하는 부트스트래퍼.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>사용자가 인증되었으나 워크스페이스 ID가 URL에 없는 경우를 감지</li>
 *   <li>워크스페이스가 하나도 없으면 온보딩(/workspaces) 스크립트를 동적 주입</li>
 *   <li>워크스페이스가 있으면 첫 번째 항목의 대시보드로 자동 선택</li>
 * </ul></p>
 */
@Singleton
public class WorkspaceOnboardingBootstrapper {
    private final UserProvider userProvider;
    private final WorkspaceList workspaceList;
    private final UriStore uri;
    private final MenuSelected menuSelected;
    private final ModuleScriptManager scriptManager;
    private boolean bootstrapped = false;
    private boolean userLoaded = false;
    private List<Workspace> loadedWorkspaces = null;

    @Inject
    WorkspaceOnboardingBootstrapper(
            UserProvider userProvider,
            WorkspaceList workspaceList,
            UriStore uri,
            MenuSelected menuSelected,
            ModuleScriptManager scriptManager
    ) {
        this.userProvider = userProvider;
        this.workspaceList = workspaceList;
        this.uri = uri;
        this.menuSelected = menuSelected;
        this.scriptManager = scriptManager;
    }

    public void initialize() {
        // 사용자가 로드되고, 워크스페이스 목록이 로드되는 시점을 기다린다.
        userProvider.subscribe(user -> {
            if (user == null) return;
            userLoaded = true;
            checkAndRedirect();
        });
        workspaceList.subscribe(list -> {
            loadedWorkspaces = list;
            checkAndRedirect();
        });
        uri.subscribe(path -> {
            if ("/workspaces".equals(path)) {
                elemental2.dom.DomGlobal.document.body.setAttribute("data-onboarding", "true");
            } else {
                elemental2.dom.DomGlobal.document.body.removeAttribute("data-onboarding");
            }
        });
    }

    private void checkAndRedirect() {
        if (bootstrapped) return;
        
        // 현재 URL 경로
        String path = uri.getValue();
        if (path == null) return;

        // 이미 특정 워크스페이스 컨텍스트 내부에 있거나 온보딩 화면이면 중단
        if (path.contains("/workspace/") || "/workspaces".equals(path)) {
            bootstrapped = true;
            return;
        }

        // 사용자와 워크스페이스 목록이 모두 준비되었는지 확인
        if (!userLoaded || loadedWorkspaces == null) return;

        bootstrapped = true;
        if (loadedWorkspaces.isEmpty()) {
            // 워크스페이스 없음 -> 메뉴 선택을 초기화하고 온보딩(생성/조인) 모듈 스크립트를 직접 주입
            menuSelected.next(null);
            scriptManager.load("/js/onboarding/onboarding.nocache.js");
            uri.next("/workspaces");
        } else {
            // 워크스페이스 있음 -> 첫 번째 항목 선택 (대시보드로 진입)
            String firstId = loadedWorkspaces.get(0).id();
            uri.next("/workspace/" + firstId + "/dashboard");
        }
    }
}
