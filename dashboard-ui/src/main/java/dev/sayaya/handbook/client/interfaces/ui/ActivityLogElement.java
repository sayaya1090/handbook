package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.usecase.AgentActivityList;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 에이전트 활동 타임라인 UI 요소.
 *
 * <p><b>책임:</b> AgentActivityList를 구독하여 에이전트 이벤트를 시간순 타임라인으로 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentActivityList} — 에이전트 활동 목록 상태 구독</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 * <p><b>주의:</b> formatTimestamp()는 JSNI로 구현되어 브라우저 Date 객체를 사용한다.</p>
 */
@Singleton
public class ActivityLogElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final elemental2.dom.HTMLDivElement listContainer;
    private Labels labels = Labels.empty();

    @Inject
    public ActivityLogElement(AgentActivityList agentActivityList, LabelProvider labelProvider) {
        var header = div().css("dash-panel-header").element();
        labelProvider.subscribe(l -> {
            this.labels = l;
            header.textContent = l.getOrDefault("dashboard.activity.title", "Agent Activity");
        });

        listContainer = div().css("dash-activity-list").element();

        element = div().css("dash-activity-panel")
                .add(header)
                .add(listContainer)
                .element();

        agentActivityList.subscribe(this::renderActivities);
    }

    private void renderActivities(List<AgentActivity> activities) {
        listContainer.innerHTML = "";
        if (activities == null || activities.isEmpty()) {
            var empty = div().css("dash-activity-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.activity.empty", "No agent activity");
            listContainer.appendChild(empty);
            return;
        }
        for (AgentActivity activity : activities) {
            var time = span().css("dash-activity-time").element();
            time.textContent = formatTimestamp(activity.timestamp);

            var status = span().css("dash-activity-status", "dash-status-" + activity.status.toLowerCase()).element();
            status.textContent = "[" + activity.status + "]";

            var intent = span().css("dash-activity-intent").element();
            intent.textContent = activity.intent + " (" + activity.commandCount + labels.getOrDefault("dashboard.activity.count_suffix", "commands") + ")";

            var row = div().css("dash-activity-row")
                    .add(time)
                    .add(status)
                    .add(intent)
                    .element();
            listContainer.appendChild(row);
        }
    }

    private static native String formatTimestamp(double ts) /*-{
        var d = new Date(ts);
        var h = ('0' + d.getHours()).slice(-2);
        var m = ('0' + d.getMinutes()).slice(-2);
        return h + ':' + m;
    }-*/;

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
