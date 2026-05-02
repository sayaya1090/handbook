package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.AgentMutation;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.WorkspaceEvent;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;

import javax.inject.Singleton;

/**
 * 외부 시스템(에이전트, SSE)과의 통신 채널을 관리하는 모듈.
 */
@Module
public interface EventModule {
    @Provides @Singleton static MutationReceiver mutationReceiver() { return AgentMutation.receiver(); }
    @Provides @Singleton static WorkspaceEventReceiver workspaceEventReceiver() { return WorkspaceEvent.receiver(); }
}
