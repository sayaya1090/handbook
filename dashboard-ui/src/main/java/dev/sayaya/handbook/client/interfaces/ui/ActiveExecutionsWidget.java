package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.ExecutionStatusData;
import dev.sayaya.handbook.client.usecase.ExecutionStatusList;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 활성 에이전트 실행 목록 위젯.
 *
 * <p><b>책임:</b> ExecutionStatusList를 구독하여 활성 에이전트 실행의 의도(intent),
 * 진행률(currentGroup/totalGroups), 상태 배지를 목록으로 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ExecutionStatusList} — 활성 실행 목록 상태 구독</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 * <p><b>주의:</b> 진행률 바는 CSS width를 퍼센트로 직접 설정한다.</p>
 */
@Singleton
public class ActiveExecutionsWidget implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> _this = div();
    private final elemental2.dom.HTMLDivElement listContainer;
    private Labels labels = Labels.empty();

    @Inject
    public ActiveExecutionsWidget(ExecutionStatusList executionStatusList, LabelProvider labelProvider) {
        var header = div().css("dash-panel-header").element();
        labelProvider.subscribe(l -> {
            this.labels = l;
            header.textContent = l.getOrDefault("dashboard.executions.title", "Active Agent Executions");
        });

        listContainer = div().css("dash-executions-list").element();

        _this.css("dash-active-executions")
                .add(header)
                .add(listContainer);

        executionStatusList.subscribe(this::renderExecutions);
    }

    private void renderExecutions(List<ExecutionStatusData> executions) {
        listContainer.innerHTML = "";
        if (executions == null || executions.isEmpty()) {
            var empty = div().css("dash-executions-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.executions.empty", "No active executions");
            listContainer.appendChild(empty);
            return;
        }
        for (ExecutionStatusData exec : executions) {
            var intent = span().css("dash-exec-intent").element();
            intent.textContent = exec.intent;

            var statusBadge = span().css("dash-exec-status", "dash-status-" + exec.status.toLowerCase()).element();
            statusBadge.textContent = exec.status;

            var progressText = span().css("dash-exec-progress-text").element();
            progressText.textContent = exec.currentGroup + "/" + exec.totalGroups;

            var progressFill = div().css("dash-exec-progress-fill").element();
            int pct = exec.totalGroups > 0 ? (int) (exec.progress * 100) : 0;
            progressFill.style.set("width", pct + "%");

            var progressBar = div().css("dash-exec-progress-bar")
                    .add(progressFill)
                    .element();

            var row = div().css("dash-exec-row")
                    .add(intent)
                    .add(progressBar)
                    .add(progressText)
                    .add(statusBadge)
                    .element();
            listContainer.appendChild(row);
        }
    }

}
