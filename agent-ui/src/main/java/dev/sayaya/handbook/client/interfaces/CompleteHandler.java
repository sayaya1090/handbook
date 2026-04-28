package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.CompleteInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.rx.Observer;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * complete 커맨드를 처리하여 프로그레스를 숨기고 완료 토스트를 표시하는 핸들러.
 *
 * <p><b>책임:</b> 완료 커맨드 수신 시 Shell의 프로그레스 바를 숨기고, ToastContainer로 완료 요약을 SUCCESS 토스트로 표시한다.
 * 아티팩트가 있는 경우 변경 건수를 포함한 상세 메시지를 표시한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 완료 스트림 구독</li>
 *   <li>{@link Observer}&lt;{@link Progress}&gt; — Shell 프로그레스 바 제어</li>
 *   <li>{@link ToastContainer} — 완료 메시지 표시</li>
 *   <li>{@link LabelProvider} — 다국어 처리</li>
 * </ul></p>
 */
@Singleton
public class CompleteHandler implements IsElement<HTMLDivElement> {
    private final ToastContainer toast = new ToastContainer();
    private Labels labels = Labels.empty();

    @Inject
    CompleteHandler(AgentCommandDispatcher dispatcher, Observer<Progress> progress, LabelProvider labelProvider) {
        labelProvider.subscribe(l -> this.labels = l);
        dispatcher.completions().subscribe(info -> {
            if (info == null) return;
            progress.next(Progress.hide());
            String message = buildMessage(info);
            toast.show(ToastLevel.SUCCESS, message, 5000);
        });
    }

    private String buildMessage(CompleteInfo info) {
        if (!info.hasArtifact()) {
            return info.summary();
        }
        int count = info.artifact().changeCount();
        if (count > 0) {
            // e.g. "Complete: 3 changes"
            String template = labels.getOrDefault("agent.complete.with_changes", "Complete: {0} changes");
            return template.replace("{0}", String.valueOf(count));
        }
        return info.summary();
    }

    @Override
    public HTMLDivElement element() { return toast.element(); }
}
