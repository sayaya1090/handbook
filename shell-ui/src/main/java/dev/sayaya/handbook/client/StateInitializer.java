package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.usecase.SessionPollingService;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;
import dev.sayaya.handbook.client.usecase.WorkspaceEventPublisher;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.LabelSharing;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StateInitializer {
    private final WorkspaceEventListener workspaceEventListener;
    private final WorkspaceEventPublisher workspaceEventPublisher;
    private final SessionPollingService sessionManager;
    private final LabelProvider labelProvider;

    @Inject
    public StateInitializer(
            WorkspaceEventListener workspaceEventListener,
            WorkspaceEventPublisher workspaceEventPublisher,
            SessionPollingService sessionManager,
            LabelProvider labelProvider
    ) {
        this.workspaceEventListener = workspaceEventListener;
        this.workspaceEventPublisher = workspaceEventPublisher;
        this.sessionManager = sessionManager;
        this.labelProvider = labelProvider;
    }

    public void initialize() {
        workspaceEventListener.initialize();
        workspaceEventPublisher.initialize();
        sessionManager.initialize();
        labelProvider.subscribe(LabelSharing::publish);
    }
}

