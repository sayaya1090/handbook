package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.interfaces.AgentSessionImpl;
import dev.sayaya.handbook.client.interfaces.AgentSseClient;
import dev.sayaya.handbook.client.interfaces.CommandRouter;
import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;

@Module
public abstract class AgentModule {
    @Binds abstract AgentSession agentSession(AgentSessionImpl impl);
    @Binds abstract AgentCommandDispatcher commandDispatcher(CommandRouter impl);
    @Binds abstract AgentApiPort agentApi(AgentSseClient impl);
}
