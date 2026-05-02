package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.OverlayContainer;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.domain.OverlayRequest;
import dev.sayaya.handbook.domain.OverlayStyle;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * attention 커맨드를 범용 OverlayContainer에 위임하는 UI 요소.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 overlayRequests를 구독하고, OverlayContainer로 coachmark/spotlight/pulse/arrow/badge 오버레이를 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 오버레이 요청 스트림 구독</li>
 *   <li>{@link OverlayContainer} — 범용 오버레이 렌더링</li>
 * </ul></p>
 */
@Singleton
public class OverlayElement implements IsElement<HTMLDivElement> {
    private final OverlayContainer overlay = new OverlayContainer();

    @Inject
    OverlayElement(AgentCommandDispatcher dispatcher) {
        dispatcher.overlayRequests().subscribe(this::handle);
    }

    private void handle(OverlayRequest request) {
        if (request == null) {
            overlay.hide();
            return;
        }
        OverlayStyle style = OverlayStyle.valueOf(request.style().name());
        overlay.show(request.target(), style, request.message(), request.position(), request.dismissable());
    }

    @Override
    public HTMLDivElement element() { return overlay.element(); }
}
