package dev.sayaya.handbook.client;

import javax.inject.Singleton;

/**
 * agent-ui 독립 실행 시 사용하는 Dagger 컴포넌트.
 *
 * <p>shell-ui 와 같은 Dagger 그래프를 공유하지 않으며, shell 이 window 브릿지에
 * 게시한 공유 상태를 {@link AgentBridgeModule} 을 통해 주입받는다.</p>
 */
@Singleton
@dagger.Component(modules = {
        AgentModule.class,
        AgentBridgeModule.class
})
public interface AgentComponent {
    AgentInitializer initializer();
}
