package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.interfaces.AgentSessionImpl;
import dev.sayaya.handbook.client.interfaces.AgentSseClient;
import dev.sayaya.handbook.client.interfaces.CommandRouter;
import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;

/**
 * 에이전트 모듈의 Dagger 바인딩 모듈.
 *
 * <p><b>책임:</b> 에이전트 유스케이스 포트를 interfaces 계층 구현체에 바인딩한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentSessionImpl} → {@link AgentSession}</li>
 *   <li>{@link CommandRouter} → {@link AgentCommandDispatcher}</li>
 *   <li>{@link AgentSseClient} → {@link AgentApiPort}</li>
 * </ul></p>
 */
@Module
public abstract class AgentModule {
    @Binds abstract AgentSession agentSession(AgentSessionImpl impl);
    @Binds abstract AgentCommandDispatcher commandDispatcher(CommandRouter impl);
    @Binds abstract AgentApiPort agentApi(AgentSseClient impl);
}
