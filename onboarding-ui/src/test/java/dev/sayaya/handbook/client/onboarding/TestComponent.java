package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.usecase.AgentWorkspaceHandler;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { dev.sayaya.handbook.client.usecase.MockModule.class })
public interface TestComponent {
    ContentElement contentElement();
    AgentWorkspaceHandler agentWorkspaceHandler();
}
