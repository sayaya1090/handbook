package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.SearchVisualizationRequest;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.rx.Observer;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * search 커맨드를 수신하여 검색 과정을 시각적으로 오케스트레이션하는 핸들러.
 *
 * <p><b>책임:</b> 에이전트가 데이터를 검색할 때 navigate → highlight(순차 강조) → preview 시퀀스를
 * 타이밍을 두고 실행하여, 사용자에게 "동료가 검색하는 모습"을 보여준다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link AgentCommandDispatcher} — search 시각화 스트림 구독</li>
 *   <li>{@link Observer}&lt;String&gt; — Shell URI Observer (화면 전환)</li>
 *   <li>{@link Observer}&lt;{@link Progress}&gt; — Shell 프로그레스 바 제어</li>
 *   <li>{@link dev.sayaya.handbook.client.components.HighlightEffect} — DOM 요소 강조 효과</li>
 *   <li>{@link LabelProvider} — 진행률/결과 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 각 단계 사이에 타이머 딜레이를 두어 시각적 흐름을 만든다.
 * navigate(0ms) → highlight(800ms 간격 순차) → preview(마지막 highlight 후 600ms).</p>
 */
@Singleton
public class SearchVisualizationHandler {
    private static final int NAVIGATE_DELAY_MS = 500;
    private static final int HIGHLIGHT_INTERVAL_MS = 800;
    private static final int PREVIEW_DELAY_MS = 600;

    private final dev.sayaya.handbook.client.components.HighlightEffect effect =
            new dev.sayaya.handbook.client.components.HighlightEffect();
    private Labels labels = Labels.empty();

    @Inject
    SearchVisualizationHandler(AgentCommandDispatcher dispatcher,
                               Observer<String> uri,
                               Observer<Progress> progress,
                               LabelProvider labelProvider) {
        labelProvider.subscribe(l -> this.labels = l);
        dispatcher.searchVisualizations().subscribe(req -> {
            if (req == null) return;
            orchestrate(req, uri, progress);
        });
    }

    private void orchestrate(SearchVisualizationRequest req, Observer<String> uri, Observer<Progress> progress) {
        String searchLabel = labels.getOrDefault("agent.search.searching", "Searching");
        int totalSteps = 1 + req.targets().length + 1; // navigate + highlights + preview
        progress.next(Progress.of(0, totalSteps, searchLabel + ": " + req.query()));

        // Step 1: Navigate to target page
        if (req.navigateTo() != null) {
            uri.next(req.navigateTo());
        }
        progress.next(Progress.of(1, totalSteps, searchLabel + ": " + req.query()));

        // Step 2: Highlight each target sequentially
        String[] targets = req.targets();
        for (int i = 0; i < targets.length; i++) {
            final int index = i;
            final int step = 2 + i;
            DomGlobal.setTimeout(e -> {
                effect.highlight(targets[index]);
                progress.next(Progress.of(step, totalSteps, searchLabel + ": " + req.query()));
            }, NAVIGATE_DELAY_MS + (double) HIGHLIGHT_INTERVAL_MS * index);
        }

        // Step 3: Show preview with results
        int previewDelay = NAVIGATE_DELAY_MS + HIGHLIGHT_INTERVAL_MS * targets.length + PREVIEW_DELAY_MS;
        DomGlobal.setTimeout(e -> {
            progress.next(Progress.of(totalSteps, totalSteps, searchLabel + ": " + req.query()));
            DomGlobal.setTimeout(e2 -> progress.next(Progress.hide()), 2000);
        }, previewDelay);
    }
}
