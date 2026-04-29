package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.usecase.SessionPollingService;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.LabelSharing;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StateInitializer {
    private final WorkspaceEventListener workspaceEventListener;
    private final WorkspaceOnboardingBootstrapper workspaceOnboardingBootstrapper;
    private final SessionPollingService sessionManager;
    private final LabelProvider labelProvider;

    @Inject
    public StateInitializer(
            WorkspaceEventListener workspaceEventListener,
            WorkspaceOnboardingBootstrapper workspaceOnboardingBootstrapper,
            SessionPollingService sessionManager,
            LabelProvider labelProvider
    ) {
        this.workspaceEventListener = workspaceEventListener;
        this.workspaceOnboardingBootstrapper = workspaceOnboardingBootstrapper;
        this.sessionManager = sessionManager;
        this.labelProvider = labelProvider;
    }

    public void initialize() {
        workspaceEventListener.initialize();
        workspaceOnboardingBootstrapper.initialize();
        sessionManager.initialize();
        labelProvider.subscribe(LabelSharing::publish);
    }
}

