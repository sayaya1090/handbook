package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.OnboardingModule;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.onboarding.ContentElement;
import dev.sayaya.handbook.client.usecase.AgentWorkspaceHandler;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { dev.sayaya.handbook.client.usecase.MockModule.class })
public interface TestComponent {
    ContentElement contentElement();
    AgentWorkspaceHandler agentWorkspaceHandler();
    CreateWorkspaceMode createWorkspaceMode();
    CreateWorkspaceParam createWorkspaceParam();
}
