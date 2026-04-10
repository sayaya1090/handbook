package dev.sayaya.handbook.client.agent;

import dev.sayaya.handbook.client.interfaces.*;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { AgentMock.class })
public interface Component {
    CommandRouter commandRouter();
    AgentSession agentSession();
    OverlayElement overlayElement();
    ConfirmDialogElement confirmDialogElement();
    PreviewPanelElement previewPanelElement();
    HighlightHandler highlightHandler();
    ScrollHandler scrollHandler();
    ProgressHandler progressHandler();
    NavigateHandler navigateHandler();
    NotifyHandler notifyHandler();
    CompleteHandler completeHandler();
    MutateHandler mutateHandler();
    AgentInputElement agentInputElement();
    ArtifactSummaryPanel artifactSummaryPanel();
    SearchVisualizationHandler searchVisualizationHandler();
    Observer<Progress> progressObserver();
}
