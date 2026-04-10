package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.domain.ConfirmRequest;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * await_confirm 커맨드 → 범용 ConfirmDialog 위임.
 */
@Singleton
public class ConfirmDialogElement implements IsElement<HTMLDivElement> {
    private final ConfirmDialog dialog = new ConfirmDialog();
    private ResponseCallback callback;

    @FunctionalInterface
    public interface ResponseCallback {
        void onResponse(String response);
    }

    @Inject
    ConfirmDialogElement(AgentCommandDispatcher dispatcher) {
        dispatcher.confirmRequests().subscribe(this::handle);
    }

    public void onResponse(ResponseCallback callback) {
        this.callback = callback;
    }

    private void handle(ConfirmRequest request) {
        if (request == null) return;
        dialog.show(request.description(), request.options(), option -> {
            if (callback != null) callback.onResponse(option);
        });
    }

    @Override
    public HTMLDivElement element() { return dialog.element(); }
}
