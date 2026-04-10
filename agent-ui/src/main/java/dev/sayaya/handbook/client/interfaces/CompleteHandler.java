package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.rx.Observer;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * complete 커맨드 → 프로그레스 숨김 + 범용 ToastContainer로 완료 요약 표시.
 */
@Singleton
public class CompleteHandler implements IsElement<HTMLDivElement> {
    private final ToastContainer toast = new ToastContainer();

    @Inject
    CompleteHandler(AgentCommandDispatcher dispatcher, Observer<Progress> progress) {
        dispatcher.completions().subscribe(summary -> {
            if (summary == null) return;
            progress.next(Progress.hide());
            toast.show(ToastLevel.SUCCESS, summary, 5000);
        });
    }

    @Override
    public HTMLDivElement element() { return toast.element(); }
}
