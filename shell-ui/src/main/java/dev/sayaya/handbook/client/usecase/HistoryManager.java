package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
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
 *   <li>사용자가 선택한 {@link Menu}의 대표 URL을 URI 스트림에 반영 (양방향 동기화)</li>
 *   <li>브라우저 뒤로 가기/앞으로 가기({@code popstate}) 감지 및 스트림 업데이트</li>
 * </ul></p>
 */
@Singleton
public class HistoryManager {
    private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
    private final BehaviorSubject<String> uri;
    private final MenuSelected menuSelected;

    @Inject HistoryManager(BehaviorSubject<String> uri, MenuSelected menuSelected) {
        this.uri = uri;
        this.menuSelected = menuSelected;
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

        // 초기 상태 로드
        if (uri.getValue() == null || uri.getValue().isEmpty()) {
            uri.next(window.location.pathname);
        }
    }

    private void onMenuSelected(Menu menu) {
        if (menu == null || menu.urlRegex() == null || menu.urlRegex().length == 0) return;
        String targetUrl = menu.urlRegex()[0];
        // 현재 URI와 다를 때만 업데이트하여 무한 루프 방지
        if (!targetUrl.equals(uri.getValue())) {
            uri.next(targetUrl);
        }
    }

    private void updateHistory(String path) {
        if (path == null || path.isEmpty()) return;
        // 해시(#) 제거 로직 포함: 클린 URL로 강제 전환
        if (path.startsWith("#")) path = path.substring(1);
        if (!path.startsWith("/")) path = "/" + path;

        if (!window.location.pathname.equals(path)) {
            logger.info("History.pushState(" + path + ")");
            window.history.pushState(null, DomGlobal.document.title, path);
        }
    }
}
