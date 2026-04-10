package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.DiffPanel;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * preview 커맨드 → 범용 DiffPanel 위임.
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
