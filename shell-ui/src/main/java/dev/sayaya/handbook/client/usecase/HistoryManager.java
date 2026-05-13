package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static elemental2.dom.DomGlobal.window;

/**
 * 브라우저 History API를 통해 클린 URL 변경을 관리한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>URI 스트림 변화를 {@code history.pushState} 로 주소창에 반영</li>
 *   <li>사용자가 선택한 {@link Menu}의 대표 URL을 URI 스트림에 반영 (양방향 동기화)</li>
 *   <li>브라우저 뒤로 가기/앞으로 가기({@code popstate}) 감지 및 스트림 업데이트</li>
 *   <li>{@link SessionContext}의 워크스페이스 ID 변경을 감지하여 URL 업데이트</li>
 * </ul></p>
 */
@Singleton
public class HistoryManager {
    private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
    private final BehaviorSubject<String> uri;
    private final MenuSelected menuSelected;
    private final PlaceholderResolver placeholderResolver;
    private final SessionContext sessionContext;
    private String currentWorkspaceId;

    @Inject
    public HistoryManager(UriStore uri, MenuSelected menuSelected, PlaceholderResolver placeholderResolver, SessionContext sessionContext) {
        this.uri = uri;
        this.menuSelected = menuSelected;
        this.placeholderResolver = placeholderResolver;
        this.sessionContext = sessionContext;
    }


    public void initialize() {
        // 1. URI 스트림 -> 주소창 반영
        uri.subscribe(this::updateHistory);

        // 2. 메뉴 선택 -> URI 스트림 반영 (양방향 동기화 핵심)
        menuSelected.subscribe(this::onMenuSelected);

        // 3. 브라우저 뒤로 가기 -> URI 스트림 반영
        window.onpopstate = evt -> {
            uri.next(window.location.pathname);
            return null;
        };
        
        // 4. 세션 컨텍스트(워크스페이스) 변경 감지 -> URL 조합
        sessionContext.subscribe(this::onContextChanged);

        // 초기 상태 로드
        if (uri.getValue() == null || uri.getValue().isEmpty()) {
            uri.next(window.location.pathname);
        }
    }

    private void onContextChanged(Map<String, String> values) {
        String wsId = values.get("workspaceId");
        if (wsId == null || Objects.equals(currentWorkspaceId, wsId)) return;
        
        currentWorkspaceId = wsId;
        String currentUri = uri.getValue();
        
        if (currentUri != null) {
            String oldWsId = WorkspaceEventListener.extractWorkspaceId(currentUri);
            if (oldWsId != null && !oldWsId.equals(wsId)) {
                String newUri = currentUri.replace("/workspaces/" + oldWsId, "/workspaces/" + wsId);
                if (!newUri.equals(currentUri)) uri.next(newUri);
            } else if (oldWsId == null) {
                // 워크스페이스 컨텍스트가 없는 URL에서 선택한 경우 대시보드로 이동
                uri.next("/workspaces/" + wsId + "/dashboard");
            }
        }
    }

    private void onMenuSelected(Menu menu) {
        if (menu == null) return;
        String targetUrl = placeholderResolver.resolve(menu.url());
        if (targetUrl == null || targetUrl.isEmpty()) return;

        // 현재 URI와 다를 때만 업데이트하여 무한 루프 방지
        if (!targetUrl.equals(uri.getValue())) {
            uri.next(targetUrl);
        }
    }

    private void updateHistory(String path) {
        if (path == null || path.isEmpty()) return;
        
        // 1. 전체 URL이 들어온 경우 origin 제거
        String origin = DomGlobal.window.location.origin;
        if (path.startsWith(origin)) path = path.substring(origin.length());
        
        // 2. 프로토콜 포함 여부 재검사 (다른 origin이거나 예외 케이스)
        if (path.contains("://")) {
            int pathStart = path.indexOf("/", path.indexOf("://") + 3);
            if (pathStart != -1) path = path.substring(pathStart);
        }

        // 3. 해시(#) 제거 및 선행 슬래시 보장
        if (path.startsWith("#")) path = path.substring(1);
        if (!path.startsWith("/")) path = "/" + path;

        if (!window.location.pathname.equals(path)) {
            logger.info("History.pushState(" + path + ")");
            window.history.pushState(null, DomGlobal.document.title, path);
        }
    }
}
