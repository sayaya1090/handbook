package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.ToastLevel;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * notify 커맨드 → 범용 ToastContainer 위임.
 */
@Singleton
public class NotifyHandler implements IsElement<HTMLDivElement> {
    private final ToastContainer toast = new ToastContainer();

    @Inject
    NotifyHandler(AgentCommandDispatcher dispatcher) {
        dispatcher.notifications().subscribe(info -> {
            if (info == null) return;
            ToastLevel level;
            try { level = ToastLevel.valueOf(info.level().toUpperCase()); }
            catch (Exception e) { level = ToastLevel.INFO; }
            toast.show(level, info.message());
        });
    }

    @Override
    public HTMLDivElement element() { return toast.element(); }
}
