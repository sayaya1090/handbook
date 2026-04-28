package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.domain.CompleteInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 에이전트 실행 완료 시 아티팩트 요약과 변경 목록을 표시하는 패널.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 completions를 구독하여, 아티팩트가 있는 경우
 * 변경 목록(타입 아이콘, 대상, 설명)을 포함한 패널을 화면에 표시한다.
 * 닫기 버튼으로 숨길 수 있다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 완료 스트림 구독</li>
 *   <li>{@link LabelProvider} — 다국어 처리</li>
 * </ul></p>
 * <p><b>주의:</b> 아티팩트가 없는 완료 이벤트에서는 패널을 표시하지 않는다.</p>
 */
@Singleton
public class ArtifactSummaryPanel implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLDivElement contentArea;
    private final HTMLElement titleEl;
    private Labels labels = Labels.empty();

    @Inject
    ArtifactSummaryPanel(AgentCommandDispatcher dispatcher, LabelProvider labelProvider) {
        contentArea = div().css("agent-artifact-content").element();

        HTMLElement closeBtn = span().css("agent-artifact-close", "material-symbols-outlined")
                .on(EventType.click, e -> hide())
                .element();
        closeBtn.textContent = "close";

        titleEl = span().css("agent-artifact-title").element();
        HTMLDivElement header = div().css("agent-artifact-header")
                .add(titleEl)
                .add(closeBtn)
                .element();

        root = div().css("agent-artifact-panel")
                .add(header)
                .add(contentArea)
                .element();
        root.style.set("display", "none");

        labelProvider.subscribe(l -> this.labels = l);
        dispatcher.completions().subscribe(info -> {
            if (info == null) return;
            if (info.hasArtifact()) {
                render(info);
            }
        });
    }

    private void render(CompleteInfo info) {
        contentArea.innerHTML = "";
        CompleteInfo.ArtifactInfo artifact = info.artifact();

        // Summary text
        if (artifact.summary() != null && !artifact.summary().isEmpty()) {
            HTMLDivElement summaryEl = div().css("agent-artifact-summary").element();
            summaryEl.textContent = artifact.summary();
            contentArea.appendChild(summaryEl);
        }

        // Title — 생성 시 저장한 참조 사용 (DOM 쿼리 불필요)
        titleEl.textContent = labels.getOrDefault("agent.artifact.title", "Execution Result");

        // Change list
        if (artifact.changeCount() > 0) {
            HTMLDivElement list = div().css("agent-artifact-changes").element();
            for (CompleteInfo.ChangeEntry change : artifact.changes()) {
                HTMLDivElement row = div().css("agent-artifact-change-row").element();

                String icon = typeToIcon(change.type());
                HTMLElement iconEl = span().css("agent-artifact-change-icon", "material-symbols-outlined").element();
                iconEl.textContent = icon;

                HTMLElement targetEl = span().css("agent-artifact-change-target").element();
                targetEl.textContent = change.target();

                HTMLElement descEl = span().css("agent-artifact-change-desc").element();
                descEl.textContent = change.description();

                row.appendChild(iconEl);
                row.appendChild(targetEl);
                row.appendChild(descEl);
                list.appendChild(row);
            }
            contentArea.appendChild(list);
        }

        root.style.set("display", "flex");
    }

    private static String typeToIcon(String type) {
        if (type == null) return "description";
        switch (type.toLowerCase()) {
            case "create": return "add_circle";
            case "update": return "edit";
            case "delete": return "remove_circle";
            case "move":   return "drive_file_move";
            default:       return "description";
        }
    }

    private void hide() {
        root.style.set("display", "none");
    }

    @Override
    public HTMLDivElement element() { return root; }
}
