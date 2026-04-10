package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.handbook.client.usecase.QualityIssueList;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/** 품질 이슈 목록 패널. severity 배지와 함께 이슈를 표시한다. */
@Singleton
public class QualityPanelElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final elemental2.dom.HTMLDivElement listContainer;

    @Inject
    public QualityPanelElement(QualityIssueList qualityIssueList) {
        var header = div().css("dash-panel-header").element();
        header.textContent = "품질 현황";

        listContainer = div().css("dash-quality-list").element();

        element = div().css("dash-quality-panel")
                .add(header)
                .add(listContainer)
                .element();

        qualityIssueList.subscribe(this::renderIssues);
    }

    private void renderIssues(List<QualityIssue> issues) {
        listContainer.innerHTML = "";
        if (issues == null || issues.isEmpty()) {
            var empty = div().css("dash-quality-empty").element();
            empty.textContent = "품질 이슈 없음";
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

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
