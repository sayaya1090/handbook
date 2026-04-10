package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.body;

/**
 * 에이전트 UI 모듈 초기화 담당.
 *
 * <p><b>책임:</b> 에이전트 관련 UI 요소(오버레이, 확인 다이얼로그, 프리뷰 패널, 핸들러 등)를 body에 부착하여 활성화한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link OverlayElement}, {@link ConfirmDialogElement}, {@link PreviewPanelElement} — 오버레이 UI</li>
 *   <li>{@link HighlightHandler}, {@link ScrollHandler}, {@link ProgressHandler} — DOM 효과 핸들러</li>
 *   <li>{@link NavigateHandler}, {@link NotifyHandler}, {@link CompleteHandler}, {@link MutateHandler} — 커맨드 핸들러</li>
 *   <li>{@link ArtifactSummaryPanel} — 아티팩트 요약 패널</li>
 *   <li>{@link AgentInputElement} — 사용자 입력 UI</li>
 *   <li>{@link SearchVisualizationHandler} — 검색 시각화 오케스트레이션</li>
 * </ul></p>
 */
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
    private final ArtifactSummaryPanel artifactSummaryPanel;
    private final AgentInputElement agentInputElement;
    private final SearchVisualizationHandler searchVisualizationHandler;

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
            ArtifactSummaryPanel artifactSummaryPanel,
            AgentInputElement agentInputElement,
            SearchVisualizationHandler searchVisualizationHandler
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
        this.artifactSummaryPanel = artifactSummaryPanel;
        this.agentInputElement = agentInputElement;
        this.searchVisualizationHandler = searchVisualizationHandler;
    }

    public void initialize() {
        body().add(overlayElement);
        body().add(confirmDialogElement);
        body().add(previewPanelElement);
        body().add(navigateHandler);
        body().add(notifyHandler);
        body().add(completeHandler);
        body().add(mutateHandler);
        body().add(artifactSummaryPanel);
        body().add(agentInputElement);
    }
}
