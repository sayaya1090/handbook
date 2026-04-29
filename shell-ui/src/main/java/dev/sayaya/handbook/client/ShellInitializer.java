package dev.sayaya.handbook.client;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SPA 셸의 초기화를 총괄하는 오케스트레이터.
 *
 * <p><b>책임:</b> 모든 매니저/리졸버/리스너를 기능별 초기화기(Routing, UI, State)로
 * 위임하여 초기화를 수행한다.
 * 세션 관리(SessionPollingService)도 함께 시작하여 JWT 만료 감시를 활성화한다.
 * 초기화 마지막에 window 브릿지를 게시하여 다른 GWT 모듈(agent-ui 등)이
 * shell 의 공유 상태에 접근할 수 있게 한다.</p>
 */
@Singleton
public class ShellInitializer {
    private final RoutingInitializer routingInitializer;
    private final UiInitializer uiInitializer;
    private final StateInitializer stateInitializer;

    @Inject
    public ShellInitializer(
            RoutingInitializer routingInitializer,
            UiInitializer uiInitializer,
            StateInitializer stateInitializer
    ) {
        this.routingInitializer = routingInitializer;
        this.uiInitializer = uiInitializer;
        this.stateInitializer = stateInitializer;
    }

    public void initialize() {
        routingInitializer.initialize();
        stateInitializer.initialize();
        uiInitializer.initialize();
    }
}


