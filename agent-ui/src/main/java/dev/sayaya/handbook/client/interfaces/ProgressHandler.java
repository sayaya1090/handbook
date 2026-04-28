package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.domain.ProgressInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.rx.Observer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * progress 커맨드를 Shell의 공용 프로그레스 바에 위임하는 핸들러.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 progressUpdates를 구독하고, Shell의 Observer&lt;Progress&gt;에 진행률을 발행한다.
 * 그룹 진행 정보(currentGroup/totalGroups)와 병렬 스텝 수를 활용해 설명 문자열을 생성한다.
 * 완료 시 2초 후 프로그레스 바를 자동 숨김한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 진행률 스트림 구독</li>
 *   <li>{@link Observer}&lt;{@link Progress}&gt; — Shell 프로그레스 바 제어</li>
 *   <li>{@link LabelProvider} — 진행률 설명 다국어 처리</li>
 * </ul></p>
 */
@Singleton
public class ProgressHandler {
    private Labels labels = Labels.empty();

    @Inject
    ProgressHandler(AgentCommandDispatcher dispatcher, Observer<Progress> progress, LabelProvider labelProvider) {
        labelProvider.subscribe(l -> this.labels = l);
        dispatcher.progressUpdates().subscribe(info -> {
            if (info == null) return;
            String description = buildDescription(info);
            progress.next(Progress.of(info.value(), info.max(), description));
            if (info.isComplete()) {
                elemental2.dom.DomGlobal.setTimeout(e -> progress.next(Progress.hide()), 2000);
            }
        });
    }

    private String buildDescription(ProgressInfo info) {
        int current = (int) info.value();
        int total = (int) info.max();
        // e.g. "Execution 2/3 - 3 parallel steps" or "Execution 2/3"
        String executionLabel = labels.getOrDefault("agent.progress.execution", "Execution");
        String base = executionLabel + " " + current + "/" + total;
        if (info.parallel() > 1) {
            String parallelLabel = labels.getOrDefault("agent.progress.parallel_steps", "{0} parallel steps");
            String parallelPart = parallelLabel.replace("{0}", String.valueOf(info.parallel()));
            return base + " - " + parallelPart;
        }
        return base;
    }
}
