package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.handbook.usecase.*;
import dev.sayaya.handbook.interfaces.api.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.Response;
import elemental2.dom.ResponseInit;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        // 1. 초기 도메인/유틸 로직
        testMenuLogic();
        testUserPreferences();
        testLanguageDetection();
        setupViewportObserver();

        // 2. 브릿지 등록 (로그용)
        setupBridgeListeners();

        // 3. 테스트 시나리오 자동 실행 (비동기 포함)
        runAutoTests();

        console.log("ACTIVITY_TEST_READY");
    }

    private void setupBridgeListeners() {
        ToolPublisher.register(tools -> console.log("LOG_TOOL_PUBLISHED:" + (tools.length > 0 ? tools[0] : "empty")));
        ToolSubscriber.register(id -> console.log("LOG_TOOL_SELECTED:" + id));
        RenderSharing.register(obj -> {
            Render render = Js.cast(obj);
            boolean result = render.onInvoke(Js.cast(DomGlobal.document.createElement("div")));
            console.log("LOG_RENDER_RECEIVED:" + result);
        });
        ProgressSharing.register(p -> console.log("LOG_PROGRESS_RECEIVED:" + p));
        LabelSharing.register(l -> console.log("LOG_LABELS_RECEIVED"));
        UriSharing.register(uri -> console.log("LOG_URI_RECEIVED:" + uri));
    }

    private void runAutoTests() {
        // 송신 테스트
        ToolPublisher.publish(new Object[]{"tool-1"});
        ToolSubscriber.select("selected-id");
        Render render = frame -> frame != null;
        RenderSharing.next(render);
        ProgressSharing.next("working-state");
        UriSharing.navigate("/test/path");

        // 실제 네트워크 호출 테스트
        FetchApi realFetch = (url, param) -> DomGlobal.window.fetch(url, param);
        new FetchLanguagePackRepository(realFetch).load("en").subscribe(l -> {
            if (l != null) console.log("LOG_FETCH_SUCCESS");
        });
    }

    private void testMenuLogic() {
        Menu menu = Menu.builder()
                .allowedSessionStates(new String[]{SessionStateKind.AUTHENTICATED.name()})
                .build();
        console.log("LOG_MENU_AUTH_ALLOWED:" + menu.isAllowedFor(SessionStateKind.AUTHENTICATED));
    }

    private void testUserPreferences() {
        UserPreferences.setLanguage("ko-KR");
        UserPreferences.setTheme("dark");
        console.log("LOG_PREF_LANG:" + UserPreferences.getLanguage());
        console.log("LOG_PREF_THEME:" + UserPreferences.getTheme());
    }

    private void testLanguageDetection() {
        BrowserLanguageDetector detector = new BrowserLanguageDetector();
        console.log("LOG_DETECTED_LANG:" + detector.detect());
    }

    private void setupViewportObserver() {
        ViewportObserver observer = new ViewportObserver();
        observer.isMobile().subscribe(isMobile -> console.log("LOG_VIEWPORT_MOBILE:" + isMobile));
    }
}
