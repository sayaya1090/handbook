package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.SessionManager;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.body;

/**
 * SPA 셸의 초기화를 총괄하는 오케스트레이터.
 *
 * <p><b>책임:</b> 모든 매니저/리졸버/리스너를 초기화하고 DOM 요소를 body에 추가한다.
 * 세션 관리(SessionManager)도 함께 시작하여 JWT 만료 감시를 활성화한다.</p>
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
 * </ul></p>
 */
@Singleton
public class ShellInitializer {
    private final HistoryManager historyManager;
    private final UrlBasedMenuResolver urlBasedMenuResolver;
    private final ToolBasedMenuResolver toolBasedMenuResolver;
    private final FrameUpdater frameUpdater;
    private final ModuleScriptManager scriptManager;
    private final ProgressElement progressElement;
    private final ContentElement contentElement;
    private final WorkspaceEventListener workspaceEventListener;
    private final SessionManager sessionManager;

    @Inject ShellInitializer(
            HistoryManager historyManager,
            UrlBasedMenuResolver urlBasedMenuResolver,
            ToolBasedMenuResolver toolBasedMenuResolver,
            FrameUpdater frameUpdater,
            ModuleScriptManager scriptManager,
            ProgressElement progressElement,
            ContentElement contentElement,
            WorkspaceEventListener workspaceEventListener,
            SessionManager sessionManager
    ) {
        this.historyManager = historyManager;
        this.urlBasedMenuResolver = urlBasedMenuResolver;
        this.toolBasedMenuResolver = toolBasedMenuResolver;
        this.frameUpdater = frameUpdater;
        this.scriptManager = scriptManager;
        this.progressElement = progressElement;
        this.contentElement = contentElement;
        this.workspaceEventListener = workspaceEventListener;
        this.sessionManager = sessionManager;
    }

    public void initialize() {
        historyManager.initialize();
        urlBasedMenuResolver.initialize();
        toolBasedMenuResolver.initialize();
        frameUpdater.initialize();
        scriptManager.initialize();
        workspaceEventListener.initialize();
        sessionManager.initialize();
        body().add(progressElement);
        body().add(contentElement);
    }
}
