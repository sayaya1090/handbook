package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.DiffPanel;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * preview 커맨드를 범용 DiffPanel에 위임하는 UI 요소.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 previewRequests를 구독하고, DiffPanel로 변경사항 미리보기를 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 프리뷰 요청 스트림 구독</li>
 *   <li>{@link DiffPanel} — 범용 변경사항 렌더링</li>
 * </ul></p>
 */
@Singleton
public class PreviewPanelElement implements IsElement<HTMLDivElement> {
    private final DiffPanel panel = new DiffPanel();

    @Inject
    PreviewPanelElement(AgentCommandDispatcher dispatcher) {
        dispatcher.previewRequests().subscribe(changes -> {
            if (changes == null) { panel.hide(); return; }
            panel.show(changes);
        });
    }

    @Override
    public HTMLDivElement element() { return panel.element(); }
}
