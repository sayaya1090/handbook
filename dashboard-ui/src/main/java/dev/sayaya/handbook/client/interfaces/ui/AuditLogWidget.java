package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.usecase.AgentActivityList;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.span;

/**
 * 사용자 변경 및 에이전트 활동을 통합 타임라인으로 표시하는 감사 로그 위젯.
 *
 * <p><b>책임:</b> AgentActivityList를 구독하여 timestamp, user/agent name, action type,
 * target, description을 포함한 통합 타임라인을 렌더링한다.
 * 날짜 범위 필터와 사용자 필터(텍스트 입력)를 제공한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link AgentActivityList} — 에이전트 활동 목록 상태 구독</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 날짜 필터는 HTML5 date input을 사용하며, 사용자 필터는
 * 대소문자 무시 부분 일치로 intent 필드를 검색한다.</p>
 */
@Singleton
public class AuditLogWidget implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> _this = div();
    private final elemental2.dom.HTMLDivElement listContainer;
    private final elemental2.dom.HTMLInputElement dateFromInput;
    private final elemental2.dom.HTMLInputElement dateToInput;
    private final elemental2.dom.HTMLInputElement userFilterInput;
    private Labels labels = Labels.empty();
    private List<AgentActivity> currentActivities;

    @Inject
    public AuditLogWidget(AgentActivityList agentActivityList, LabelProvider labelProvider) {
        var header = div().css("dash-panel-header").element();
        labelProvider.subscribe(l -> {
            this.labels = l;
            header.textContent = l.getOrDefault("dashboard.audit.title", "Audit Log");
        });

        // 필터 컨트롤: 날짜 범위
        dateFromInput = (elemental2.dom.HTMLInputElement) elemental2.dom.DomGlobal.document.createElement("input");
        dateFromInput.type = "date";
        dateFromInput.classList.add("dash-audit-filter-input");
        dateFromInput.addEventListener("change", e -> applyFilters());

        dateToInput = (elemental2.dom.HTMLInputElement) elemental2.dom.DomGlobal.document.createElement("input");
        dateToInput.type = "date";
        dateToInput.classList.add("dash-audit-filter-input");
        dateToInput.addEventListener("change", e -> applyFilters());

        // 필터 컨트롤: 사용자 필터
        userFilterInput = (elemental2.dom.HTMLInputElement) elemental2.dom.DomGlobal.document.createElement("input");
        userFilterInput.type = "text";
        userFilterInput.classList.add("dash-audit-filter-input");
        userFilterInput.addEventListener("input", e -> applyFilters());

        var dateFromLabel = span().css("dash-audit-filter-label").element();
        var dateToLabel = span().css("dash-audit-filter-label").element();
        var userLabel = span().css("dash-audit-filter-label").element();
        labelProvider.subscribe(l -> {
            dateFromLabel.textContent = l.getOrDefault("dashboard.audit.from", "From");
            dateToLabel.textContent = l.getOrDefault("dashboard.audit.to", "To");
            userLabel.textContent = l.getOrDefault("dashboard.audit.user_filter", "User");
            userFilterInput.placeholder = l.getOrDefault("dashboard.audit.user_placeholder", "Filter by user...");
        });

        var filters = div().css("dash-audit-filters").element();
        filters.appendChild(dateFromLabel);
        filters.appendChild(dateFromInput);
        filters.appendChild(dateToLabel);
        filters.appendChild(dateToInput);
        filters.appendChild(userLabel);
        filters.appendChild(userFilterInput);

        listContainer = div().css("dash-audit-list").element();

        _this.css("dash-audit-log")
                .add(header)
                .add(filters)
                .add(listContainer);

        agentActivityList.subscribe(activities -> {
            this.currentActivities = activities;
            applyFilters();
        });
    }

    private void applyFilters() {
        listContainer.innerHTML = "";
        if (currentActivities == null || currentActivities.isEmpty()) {
            var empty = div().css("dash-audit-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.audit.empty", "No audit log entries");
            listContainer.appendChild(empty);
            return;
        }

        String userFilter = userFilterInput.value != null ? userFilterInput.value.trim().toLowerCase() : "";
        double fromMs = parseDate(dateFromInput.value, true);
        double toMs = parseDate(dateToInput.value, false);

        for (AgentActivity activity : currentActivities) {
            // 날짜 범위 필터
            if (fromMs > 0 && activity.timestamp < fromMs) continue;
            if (toMs > 0 && activity.timestamp > toMs) continue;

            // 사용자 필터: intent 필드를 대소문자 무시 부분 일치
            if (!userFilter.isEmpty() && (activity.intent == null ||
                    !activity.intent.toLowerCase().contains(userFilter))) continue;

            renderRow(activity);
        }

        if (listContainer.childElementCount == 0) {
            var empty = div().css("dash-audit-empty").element();
            empty.textContent = labels.getOrDefault("dashboard.audit.no_match", "No matching entries");
            listContainer.appendChild(empty);
        }
    }

    private void renderRow(AgentActivity activity) {
        var time = span().css("dash-audit-time").element();
        time.textContent = formatTimestamp(activity.timestamp);

        var status = span().css("dash-audit-status", "dash-status-" + activity.status.toLowerCase()).element();
        status.textContent = activity.status;

        var action = span().css("dash-audit-action").element();
        action.textContent = activity.intent;

        var count = span().css("dash-audit-count").element();
        count.textContent = activity.commandCount + " " +
                labels.getOrDefault("dashboard.audit.commands", "commands");

        var row = div().css("dash-audit-row")
                .add(time)
                .add(status)
                .add(action)
                .add(count)
                .element();
        listContainer.appendChild(row);
    }

    /** 타임스탬프(ms)를 "yyyy-MM-dd HH:mm" 형식으로 변환한다. */
    private static String formatTimestamp(double ts) {
        elemental2.core.JsDate d = new elemental2.core.JsDate(ts);
        int y = (int) d.getFullYear();
        int mon = (int) d.getMonth() + 1;
        int day = (int) d.getDate();
        int h = (int) d.getHours();
        int m = (int) d.getMinutes();
        return y + "-"
             + (mon < 10 ? "0" + mon : "" + mon) + "-"
             + (day < 10 ? "0" + day : "" + day) + " "
             + (h < 10 ? "0" + h : "" + h) + ":"
             + (m < 10 ? "0" + m : "" + m);
    }

    /**
     * 날짜 문자열(yyyy-MM-dd)을 밀리초 타임스탬프로 변환한다.
     *
     * @param dateStr HTML date input 값 (yyyy-MM-dd 형식)
     * @param startOfDay true이면 00:00:00.000, false이면 23:59:59.999로 설정
     * @return 밀리초 타임스탬프, 파싱 실패 시 0
     */
    private static double parseDate(String dateStr, boolean startOfDay) {
        if (dateStr == null || dateStr.isEmpty()) return 0;
        elemental2.core.JsDate d = new elemental2.core.JsDate(dateStr);
        if (Double.isNaN(d.getTime())) return 0;
        if (startOfDay) {
            d.setHours(0, 0, 0, 0);
        } else {
            d.setHours(23, 59, 59, 999);
        }
        return d.getTime();
    }

}
