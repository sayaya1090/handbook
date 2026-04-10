package dev.sayaya.handbook.client.create;

import dev.sayaya.handbook.client.WorkspaceModule;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.create.ContentElement;
import dev.sayaya.handbook.client.usecase.AgentWorkspaceHandler;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { WorkspaceModule.class, ApiModule.class })
public interface TestComponent {
    ContentElement contentElement();
    AgentWorkspaceHandler agentWorkspaceHandler();
    CreateWorkspaceMode createWorkspaceMode();
    CreateWorkspaceParam createWorkspaceParam();
}
