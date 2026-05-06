package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.Observer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 참여 중인 워크스페이스가 있는 경우 루트 경로(/)에서 자동으로 첫 번째 워크스페이스로 이동시킨다.
 * (요구사항 §2.1.2: 마지막 액션 워크스페이스 진입의 Phase 1 구현)
 */
@Singleton
public class HomeRedirector {
    private final UriStore uriStore;
    private final WorkspaceList workspaceList;
    private final Observer<String> uriObserver;

    @Inject
    public HomeRedirector(UriStore uriStore, WorkspaceList workspaceList, Observer<String> uriObserver) {
        this.uriStore = uriStore;
        this.workspaceList = workspaceList;
        this.uriObserver = uriObserver;
    }

    public void initialize() {
        // 경로와 워크스페이스 목록 중 하나라도 변경되면 리다이렉트 여부 판단
        uriStore.subscribe(uri -> redirectIfHome());
        workspaceList.subscribe(list -> redirectIfHome());
    }

    private void redirectIfHome() {
        String currentPath = uriStore.getValue();
        List<Workspace> workspaces = workspaceList.getValue();
        
        // 1. 루트 경로(/) 인지 확인 (null 또는 empty 도 루트로 간주)
        boolean isHome = currentPath == null || currentPath.isEmpty() 
                || "/".equals(currentPath) 
                || "/app.html".equals(currentPath)
                || "/redirecttest.html".equals(currentPath);
        if (!isHome) return;

        // 2. 참여 중인 워크스페이스가 있는지 확인
        if (workspaces == null || workspaces.isEmpty()) return;

        // 3. 첫 번째 워크스페이스 대시보드로 이동
        // TODO: 추후 '마지막 진입 워크스페이스' 저장 로직 도입 시 해당 ID를 우선 사용하도록 개선
        String firstWsId = workspaces.get(0).id();
        String targetUri = "/workspaces/" + firstWsId + "/dashboard";
        
        uriObserver.next(targetUri);
    }
}
