package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.LabelSharing;
import dev.sayaya.handbook.usecase.ProgressSharing;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.handbook.usecase.UriSharing;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

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
    @SuppressWarnings("unused") private final MobileTabsPresenter mobileTabsPresenter;
    private final ProgressElement progressElement;
    private final ContentElement contentElement;
    private final WorkspaceEventListener workspaceEventListener;
    private final WorkspaceOnboardingBootstrapper workspaceOnboardingBootstrapper;
    private final SessionPollingService sessionManager;
    private final BehaviorSubject<Progress> progress;
    private final BehaviorSubject<Render> render;
    private final BehaviorSubject<String> uri;
    private final LabelProvider labelProvider;

    @Inject
    public ShellInitializer(
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
            ProgressStore progress,
            RenderStore render,
            UriStore uri,
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
        this.progress = progress;
        this.render = render;
        this.uri = uri;
        this.labelProvider = labelProvider;
    }

    public void initialize() {
        historyManager.initialize();
        urlBasedMenuResolver.initialize();
        toolBasedMenuResolver.initialize();
        frameUpdater.initialize();
        scriptManager.initialize();
        workspaceEventListener.initialize();
        workspaceOnboardingBootstrapper.initialize();
        sessionManager.initialize();

        body().add(appBar);
        body().add(mobileTabs);
        body().add(progressElement);
        body().add(contentElement);
        publishBridges();
    }

    private void publishBridges() {
        ProgressSharing.register(value -> progress.next((Progress)Js.cast(value)));
        RenderSharing.register(value -> { Render r = Js.cast(value); render.next(r); });
        UriSharing.register(uri::next);
        labelProvider.subscribe(LabelSharing::publish);
        DomGlobal.window.dispatchEvent(new CustomEvent<>("handbook-shell-ready"));
    }
}
