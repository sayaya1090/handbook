package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsElement;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.SessionManager;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.WindowLabelBridge;
import dev.sayaya.handbook.usecase.WindowProgressBridge;
import dev.sayaya.handbook.usecase.WindowRenderBridge;
import dev.sayaya.handbook.usecase.WindowUriBridge;
import dev.sayaya.rx.Observer;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.body;

/**
 * SPA 셸의 초기화를 총괄하는 오케스트레이터.
 *
 * <p><b>책임:</b> 모든 매니저/리졸버/리스너를 초기화하고 DOM 요소를 body에 추가한다.
 * 세션 관리(SessionManager)도 함께 시작하여 JWT 만료 감시를 활성화한다.
 * 초기화 마지막에 window 브릿지를 게시하여 다른 GWT 모듈(agent-ui 등)이
 * shell 의 공유 상태에 접근할 수 있게 한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link HistoryManager} — URL 히스토리 관리</li>
 *   <li>{@link UrlBasedMenuResolver} — URL 기반 메뉴 해석</li>
 *   <li>{@link ToolBasedMenuResolver} — 도구 기반 메뉴 해석</li>
 *   <li>{@link FrameUpdater} — 프레임 갱신</li>
 *   <li>{@link ModuleScriptManager} — 모듈 스크립트 동적 로딩</li>
 *   <li>{@link SessionManager} — JWT 세션 감시 및 자동 갱신</li>
 *   <li>{@link WorkspaceEventListener} — 워크스페이스 SSE 이벤트 구독</li>
 *   <li>{@link Observer}&lt;Progress&gt; — 프로그레스 브릿지 등록용</li>
 *   <li>{@link Observer}&lt;String&gt; — URI 브릿지 등록용</li>
 *   <li>{@link LabelProvider} — 레이블 브릿지 등록용</li>
 * </ul></p>
 */
@Singleton
public class ShellInitializer {
    private final HistoryManager historyManager;
    private final UrlBasedMenuResolver urlBasedMenuResolver;
    private final ToolBasedMenuResolver toolBasedMenuResolver;
    private final FrameUpdater frameUpdater;
    private final ModuleScriptManager scriptManager;
    private final ShellAppBarElement appBar;
    private final MobileTabsElement mobileTabs;
    private final ProgressElement progressElement;
    private final ContentElement contentElement;
    private final WorkspaceEventListener workspaceEventListener;
    private final SessionManager sessionManager;
    private final Observer<Progress> progressObserver;
    private final Observer<Render> renderObserver;
    private final Observer<String> uriObserver;
    private final LabelProvider labelProvider;

    @Inject ShellInitializer(
            HistoryManager historyManager,
            UrlBasedMenuResolver urlBasedMenuResolver,
            ToolBasedMenuResolver toolBasedMenuResolver,
            FrameUpdater frameUpdater,
            ModuleScriptManager scriptManager,
            ShellAppBarElement appBar,
            MobileTabsElement mobileTabs,
            ProgressElement progressElement,
            ContentElement contentElement,
            WorkspaceEventListener workspaceEventListener,
            SessionManager sessionManager,
            Observer<Progress> progressObserver,
            Observer<Render> renderObserver,
            Observer<String> uriObserver,
            LabelProvider labelProvider
    ) {
        this.historyManager = historyManager;
        this.urlBasedMenuResolver = urlBasedMenuResolver;
        this.toolBasedMenuResolver = toolBasedMenuResolver;
        this.frameUpdater = frameUpdater;
        this.scriptManager = scriptManager;
        this.appBar = appBar;
        this.mobileTabs = mobileTabs;
        this.progressElement = progressElement;
        this.contentElement = contentElement;
        this.workspaceEventListener = workspaceEventListener;
        this.sessionManager = sessionManager;
        this.progressObserver = progressObserver;
        this.renderObserver = renderObserver;
        this.uriObserver = uriObserver;
        this.labelProvider = labelProvider;
    }

    public void initialize() {
        historyManager.initialize();
        urlBasedMenuResolver.initialize();
        toolBasedMenuResolver.initialize();
        frameUpdater.initialize();
        scriptManager.initialize();
        workspaceEventListener.initialize();
        sessionManager.initialize();
        // Composition Root — DOM 최상위 배치 순서를 한 곳에서 명시한다.
        // AppBar(최상단 고정) → MobileTabs(AppBar 바로 아래) → Progress(상단 indicator)
        // → Content(Drawer + 본문). Drawer 의 backdrop-filter 가 containing block 을
        // 오염시키지 않도록 fixed 위젯은 반드시 body 직속에 놓는다.
        body().add(appBar);
        body().add(mobileTabs);
        body().add(progressElement);
        body().add(contentElement);
        publishBridges();
    }

    private void publishBridges() {
        WindowProgressBridge.register(value -> progressObserver.next(jsToProgress(value)));
        WindowRenderBridge.register(value -> { Render r = jsinterop.base.Js.cast(value); renderObserver.next(r); });
        WindowUriBridge.register(uriObserver::next);
        labelProvider.subscribe(labels -> WindowLabelBridge.publish(labels));
        DomGlobal.window.dispatchEvent(new CustomEvent<>("handbook-shell-ready"));
    }

    @SuppressWarnings("unchecked")
    private static Progress jsToProgress(Object obj) {
        return jsinterop.base.Js.cast(obj);
    }
}
