package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.ToastLevel;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * notify 커맨드를 범용 ToastContainer에 위임하는 핸들러.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 notifications를 구독하고, ToastContainer로 레벨별 토스트 메시지를 표시한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 알림 스트림 구독</li>
 *   <li>{@link ToastContainer} — 범용 토스트 렌더링</li>
 * </ul></p>
 */
@Singleton
public class NotifyHandler implements IsElement<HTMLDivElement> {
    private final ToastContainer toast = new ToastContainer();

    @Inject
    NotifyHandler(AgentCommandDispatcher dispatcher) {
        dispatcher.notifications().subscribe(cmd -> {
            if (cmd == null) return;
            ToastLevel level;
            try { level = ToastLevel.valueOf(cmd.level().toUpperCase()); }
            catch (Exception e) { level = ToastLevel.INFO; }
            toast.show(level, cmd.message());
        });
    }

    @Override
    public HTMLDivElement element() { return toast.element(); }
}
