package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.OverlayContainer;
import dev.sayaya.handbook.client.domain.OverlayRequest;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.OverlayStyle;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * attention 커맨드 → 범용 OverlayContainer 위임.
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
