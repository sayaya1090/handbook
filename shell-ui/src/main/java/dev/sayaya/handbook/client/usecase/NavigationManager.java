package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.Observer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;

/**
 * 애플리케이션의 상태 변화(메뉴 선택, 워크스페이스 변경 등)를 감지하여 
 * 새로운 대상 URL을 결정하고 UriStore에 발행하는 라우팅 정책 관리자.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>{@link MenuSelected} 이벤트를 감지하여 해당 메뉴의 URL로 이동 지시</li>
 *   <li>{@link SessionContext}의 워크스페이스 ID 변경을 감지하여 현재 경로 내의 ID를 치환하여 이동 지시</li>
 * </ul></p>
 */
@Singleton
public class NavigationManager {
    private final UriStore uriStore;
    private final Observer<String> uriObserver;
    private final MenuSelected menuSelected;
    private final SessionContext sessionContext;
    private final PlaceholderResolver placeholderResolver;
    private String currentWorkspaceId;

    @Inject
    public NavigationManager(
            UriStore uriStore, 
            Observer<String> uriObserver, 
            MenuSelected menuSelected, 
            SessionContext sessionContext, 
            PlaceholderResolver placeholderResolver) {
        this.uriStore = uriStore;
        this.uriObserver = uriObserver;
        this.menuSelected = menuSelected;
        this.sessionContext = sessionContext;
        this.placeholderResolver = placeholderResolver;
    }

    public void initialize() {
        // 1. 메뉴 선택 시 URL 발행
        menuSelected.subscribe(this::onMenuSelected);
        
        // 2. 워크스페이스 컨텍스트 변경 시 URL 발행
        sessionContext.subscribe(this::onWorkspaceContextChanged);
    }

    private void onMenuSelected(Menu menu) {
        if (menu == null) return;
        String targetUrl = placeholderResolver.resolve(menu.url());
        if (targetUrl == null || targetUrl.isEmpty()) return;

        // 현재 URI와 다를 때만 업데이트하여 무한 루프 방지
        if (!targetUrl.equals(uriStore.getValue())) {
            uriObserver.next(targetUrl);
        }
    }

    private void onWorkspaceContextChanged(Map<String, String> values) {
        String wsId = values.get("workspaceId");
        if (wsId == null || Objects.equals(currentWorkspaceId, wsId)) return;
        
        currentWorkspaceId = wsId;
        String currentUri = uriStore.getValue();
        
        if (currentUri != null) {
            String oldWsId = WorkspaceEventListener.extractWorkspaceId(currentUri);
            if (oldWsId != null && !oldWsId.equals(wsId)) {
                String newUri = currentUri.replace("/workspaces/" + oldWsId, "/workspaces/" + wsId);
                if (!newUri.equals(currentUri)) uriObserver.next(newUri);
            } else if (oldWsId == null) {
                // 워크스페이스 컨텍스트가 없는 URL에서 선택한 경우 대시보드로 이동
                uriObserver.next("/workspaces/" + wsId + "/dashboard");
            }
        }
    }
}
