package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.body;

@Singleton
public class AgentInitializer {
    private final HighlightHandler highlightHandler;
    private final ScrollHandler scrollHandler;
    private final ProgressHandler progressHandler;
    private final OverlayElement overlayElement;
    private final ConfirmDialogElement confirmDialogElement;
    private final PreviewPanelElement previewPanelElement;
    private final NavigateHandler navigateHandler;
    private final NotifyHandler notifyHandler;
    private final CompleteHandler completeHandler;
    private final MutateHandler mutateHandler;
    private final AgentInputElement agentInputElement;

    @Inject AgentInitializer(
            HighlightHandler highlightHandler,
            ScrollHandler scrollHandler,
            ProgressHandler progressHandler,
            OverlayElement overlayElement,
            ConfirmDialogElement confirmDialogElement,
            PreviewPanelElement previewPanelElement,
            NavigateHandler navigateHandler,
            NotifyHandler notifyHandler,
            CompleteHandler completeHandler,
            MutateHandler mutateHandler,
            AgentInputElement agentInputElement
    ) {
        this.highlightHandler = highlightHandler;
        this.scrollHandler = scrollHandler;
        this.progressHandler = progressHandler;
        this.overlayElement = overlayElement;
        this.confirmDialogElement = confirmDialogElement;
        this.previewPanelElement = previewPanelElement;
        this.navigateHandler = navigateHandler;
        this.notifyHandler = notifyHandler;
        this.completeHandler = completeHandler;
        this.mutateHandler = mutateHandler;
        this.agentInputElement = agentInputElement;
    }

    public void initialize() {
        body().add(overlayElement);
        body().add(confirmDialogElement);
        body().add(previewPanelElement);
        body().add(navigateHandler);
        body().add(notifyHandler);
        body().add(completeHandler);
        body().add(mutateHandler);
        body().add(agentInputElement);
    }
}
