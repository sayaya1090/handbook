package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

import static elemental2.dom.DomGlobal.window;

/**
 * 브라우저 History API를 통해 URL 변경을 관리한다.
 * URI 변경 시 pushState를 호출하고, popstate 이벤트를 구독한다.
 */
@Singleton
public class HistoryManager {
    private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
    private final BehaviorSubject<String> uri;
    @Inject HistoryManager(BehaviorSubject<String> uri) {
        this.uri = uri;
    }
    public void initialize() {
        uri.subscribe(this::update);
        window.onpopstate = evt -> {
            uri.next(window.location.href);
            return null;
        };
    }
    private void update(String url) {
        if(url == null) return;
        if(!window.location.href.equals(url)) {
            logger.info("History.pushState(" + url + ")");
            DomGlobal.window.history.pushState("", DomGlobal.document.title, url);
        }
    }
}
