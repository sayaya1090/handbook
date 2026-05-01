package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.usecase.ArtifactList;
import dev.sayaya.handbook.domain.ArtifactData;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 최근 아티팩트 목록 위젯.
 *
 * <p><b>책임:</b> ArtifactList를 구독하여 완료된 에이전트 아티팩트의 요약(summary),
 * 변경 수(changes count), 타임스탬프를 목록으로 렌더링한다.
 * 각 행을 클릭하면 변경 상세를 펼쳐 볼 수 있다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ArtifactList} — 아티팩트 목록 상태 구독</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 * <p><b>주의:</b> formatTimestamp()는 JsDate를 사용하여 시간을 포맷한다.</p>
 */
@Singleton
public class ArtifactListWidget implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> _this = div();
    private final elemental2.dom.HTMLDivElement listContainer;
    private Labels labels = Labels.empty();

    @Inject
    public ArtifactListWidget(ArtifactList artifactList, LabelProvider labelProvider) {
        var header = div().css("dash-panel-header").element();
        labelProvider.subscribe(l -> {
            this.labels = l;
            header.textContent = l.getOrDefault("dashboard.artifacts.title", "Recent Artifacts");
        });

        listContainer = div().css("dash-artifact-items").element();

        _this.css("dash-artifact-list")
                .add(header)
                .add(listContainer);

        artifactList.subscribe(this::renderArtifacts);
    }

    private void renderArtifacts(List<ArtifactData> artifacts) {
        listContainer.innerHTML = "";
        if (artifacts == null || artifacts.isEmpty()) {
            var empty = div().css("dash-artifact-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.artifacts.empty", "No artifacts");
            listContainer.appendChild(empty);
            return;
        }
        for (ArtifactData artifact : artifacts) {
            var summary = span().css("dash-artifact-summary").element();
            summary.textContent = artifact.summary;

            int changeCount = artifact.changes != null ? artifact.changes.length : 0;
            var changes = span().css("dash-artifact-changes").element();
            changes.textContent = changeCount + " " + labels.getOrDefault("dashboard.artifacts.changes", "changes");

            var time = span().css("dash-artifact-time").element();
            time.textContent = formatTimestamp(artifact.timestamp);

            var details = div().css("dash-artifact-details").element();
            details.style.set("display", "none");
            if (artifact.changes != null) {
                for (String change : artifact.changes) {
                    var changeLine = div().css("dash-artifact-change-line").element();
                    changeLine.textContent = change;
                    details.appendChild(changeLine);
                }
            }

            var rowBuilder = div().css("dash-artifact-row")
                    .add(summary)
                    .add(changes)
                    .add(time)
                    .add(details);
            var row = rowBuilder.element();
            rowBuilder.on(EventType.click, e -> {
                boolean hidden = "none".equals(details.style.get("display"));
                details.style.set("display", hidden ? "block" : "none");
                if (hidden) row.classList.add("dash-artifact-row-expanded");
                else row.classList.remove("dash-artifact-row-expanded");
            });
            listContainer.appendChild(row);
        }
    }

    /** 타임스탬프(ms)를 "MM-dd HH:mm" 형식으로 변환한다. */
    private static String formatTimestamp(double ts) {
        elemental2.core.JsDate d = new elemental2.core.JsDate(ts);
        int month = (int) d.getMonth() + 1;
        int day = (int) d.getDate();
        int h = (int) d.getHours();
        int m = (int) d.getMinutes();
        return (month < 10 ? "0" + month : "" + month) + "-"
             + (day < 10 ? "0" + day : "" + day) + " "
             + (h < 10 ? "0" + h : "" + h) + ":"
             + (m < 10 ? "0" + m : "" + m);
    }

}
