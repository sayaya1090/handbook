package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.QualityIssue;
import dev.sayaya.handbook.client.usecase.QualityIssueList;
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
 * 품질 이슈 목록 패널 UI 요소.
 *
 * <p><b>책임:</b> QualityIssueList를 구독하여 severity 배지 + 이슈 메시지를 목록으로 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link QualityIssueList} — 품질 이슈 목록 상태 구독</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 */
@Singleton
public class QualityPanelElement implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> _this = div();
    private final elemental2.dom.HTMLDivElement listContainer;
    private Labels labels = Labels.empty();

    @Inject
    public QualityPanelElement(QualityIssueList qualityIssueList, LabelProvider labelProvider) {
        var header = div().css("dash-panel-header").element();
        labelProvider.subscribe(l -> {
            this.labels = l;
            header.textContent = l.getOrDefault("dashboard.quality.title", "Quality Status");
        });

        listContainer = div().css("dash-quality-list").element();

        _this.css("dash-quality-panel")
                .add(header)
                .add(listContainer);

        qualityIssueList.subscribe(this::renderIssues);
    }

    private void renderIssues(List<QualityIssue> issues) {
        listContainer.innerHTML = "";
        if (issues == null || issues.isEmpty()) {
            var empty = div().css("dash-quality-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.quality.empty", "No quality issues");
            listContainer.appendChild(empty);
            return;
        }
        for (QualityIssue issue : issues) {
            var badge = span().css("dash-severity-badge", "dash-severity-" + issue.severity).element();
            badge.textContent = issue.severity;

            var msg = span().css("dash-quality-message").element();
            msg.textContent = "[" + issue.type + " " + issue.serial + "] " + issue.field + ": " + issue.message;

            var row = div().css("dash-quality-row")
                    .add(badge)
                    .add(msg)
                    .element();
            listContainer.appendChild(row);
        }
    }

}
