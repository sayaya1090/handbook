package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

import static elemental2.dom.DomGlobal.window;

/**
 * 브라우저 History API를 통해 클린 URL 변경을 관리한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>URI 스트림 변화를 {@code history.pushState} 로 주소창에 반영</li>
 *   <li>브라우저 뒤로 가기/앞으로 가기({@code popstate}) 감지 및 스트림 업데이트</li>
 * </ul></p>
 */
@Singleton
public class HistoryManager {
    private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
    private final BehaviorSubject<String> uri;

    @Inject
    public HistoryManager(UriStore uri) {
        this.uri = uri;
    }

    public void initialize() {
        // 1. URI 스트림 -> 주소창 반영
        uri.subscribe(this::updateHistory);

        // 2. 브라우저 뒤로 가기 -> URI 스트림 반영
        window.onpopstate = evt -> {
            uri.next(window.location.pathname);
            return null;
        };

        // 초기 상태 로드
        if (uri.getValue() == null || uri.getValue().isEmpty()) {
            uri.next(window.location.pathname);
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
