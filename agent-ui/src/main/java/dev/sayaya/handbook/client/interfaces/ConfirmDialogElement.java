package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.domain.ConfirmRequest;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * await_confirm 커맨드를 범용 ConfirmDialog에 위임하는 UI 요소.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 confirmRequests를 구독하고, ConfirmDialog를 표시하여 사용자 선택을 콜백으로 반환한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 확인 요청 스트림 구독</li>
 *   <li>{@link ConfirmDialog} — 범용 확인 다이얼로그 렌더링</li>
 * </ul></p>
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
