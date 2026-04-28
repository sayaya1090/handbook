package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.SessionPollingService;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.LabelSharing;
import dev.sayaya.handbook.usecase.ProgressSharing;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.handbook.usecase.UriSharing;
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
 * 세션 관리(SessionPollingService)도 함께 시작하여 JWT 만료 감시를 활성화한다.
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
 *   <li>{@link SessionPollingService} — JWT 세션 감시 및 자동 갱신</li>
 *   <li>{@link WorkspaceEventListener} — 워크스페이스 SSE 이벤트 구독</li>
 *   <li>{@link Observer}&lt;Progress&gt; — 프로그레스 브릿지 등록용</li>
 *   <li>{@link Observer}&lt;String&gt; — URI 브릿지 등록용</li>
 *   <li>{@link LabelProvider} — 레이블 브릿지 등록용</li>
 * </ul></p>
 */
@Singleton
public class ShellInitializer {
    @Singleton
    public static class ShellContext {
        public final HistoryManager historyManager;
        public final UrlBasedMenuResolver urlBasedMenuResolver;
        public final ToolBasedMenuResolver toolBasedMenuResolver;
        public final FrameUpdater frameUpdater;
        public final ModuleScriptManager scriptManager;
        public final ShellAppBarElement appBar;
        public final MobileTabsElement mobileTabs;
        @SuppressWarnings("unused") public final MobileTabsPresenter mobileTabsPresenter;
        public final ProgressElement progressElement;
        public final ContentElement contentElement;
        public final WorkspaceEventListener workspaceEventListener;
        public final WorkspaceOnboardingBootstrapper workspaceOnboardingBootstrapper;
        public final SessionPollingService sessionManager;
        public final Observer<Progress> progressObserver;
        public final Observer<Render> renderObserver;
        public final Observer<String> uriObserver;
        public final LabelProvider labelProvider;

        @Inject
        public ShellContext(
                HistoryManager historyManager,
                UrlBasedMenuResolver urlBasedMenuResolver,
                ToolBasedMenuResolver toolBasedMenuResolver,
                FrameUpdater frameUpdater,
                ModuleScriptManager scriptManager,
                ShellAppBarElement appBar,
                MobileTabsElement mobileTabs,
                MobileTabsPresenter mobileTabsPresenter,
                ProgressElement progressElement,
                ContentElement contentElement,
                WorkspaceEventListener workspaceEventListener,
                WorkspaceOnboardingBootstrapper workspaceOnboardingBootstrapper,
                SessionPollingService sessionManager,
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
            this.mobileTabsPresenter = mobileTabsPresenter;
            this.progressElement = progressElement;
            this.contentElement = contentElement;
            this.workspaceEventListener = workspaceEventListener;
            this.workspaceOnboardingBootstrapper = workspaceOnboardingBootstrapper;
            this.sessionManager = sessionManager;
            this.progressObserver = progressObserver;
            this.renderObserver = renderObserver;
            this.uriObserver = uriObserver;
            this.labelProvider = labelProvider;
        }
    }

    private final ShellContext context;

    @Inject ShellInitializer(ShellContext context) {
        this.context = context;
    }

    public void initialize() {
        context.historyManager.initialize();
        context.urlBasedMenuResolver.initialize();
        context.toolBasedMenuResolver.initialize();
        context.frameUpdater.initialize();
        context.scriptManager.initialize();
        context.workspaceEventListener.initialize();
        context.workspaceOnboardingBootstrapper.initialize();
        context.sessionManager.initialize();
        // Composition Root — DOM 최상위 배치 순서를 한 곳에서 명시한다.
        // AppBar(최상단 고정) → MobileTabs(AppBar 바로 아래) → Progress(상단 indicator)
        // → Content(Drawer + 본문). Drawer 의 backdrop-filter 가 containing block 을
        // 오염시키지 않도록 fixed 위젯은 반드시 body 직속에 놓는다.
        body().add(context.appBar);
        body().add(context.mobileTabs);
        body().add(context.progressElement);
        body().add(context.contentElement);
        publishBridges();
    }

    private void publishBridges() {
        ProgressSharing.register(value -> context.progressObserver.next(jsToProgress(value)));
        RenderSharing.register(value -> { Render r = jsinterop.base.Js.cast(value); context.renderObserver.next(r); });
        0.register(context.uriObserver::next);
        context.labelProvider.subscribe(labels -> LabelSharing.publish(labels));
        DomGlobal.window.dispatchEvent(new CustomEvent<>("handbook-shell-ready"));
    }

    @SuppressWarnings("unchecked")
    private static Progress jsToProgress(Object obj) {
        return jsinterop.base.Js.cast(obj);
    }
}
