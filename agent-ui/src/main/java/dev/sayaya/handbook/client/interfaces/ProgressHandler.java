package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.ProgressInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * progress 커맨드를 Shell의 공용 프로그레스 바에 위임하는 핸들러.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 progressUpdates를 구독하고, Shell의 Observer&lt;Progress&gt;에 진행률을 발행한다. 완료 시 2초 후 프로그레스 바를 자동 숨김한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 진행률 스트림 구독</li>
 *   <li>{@link Observer}&lt;{@link Progress}&gt; — Shell 프로그레스 바 제어</li>
 * </ul></p>
 */
@Singleton
public class ProgressHandler {
    @Inject
    ProgressHandler(AgentCommandDispatcher dispatcher, Observer<Progress> progress) {
        dispatcher.progressUpdates().subscribe(info -> {
            if (info == null) return;
            progress.next(Progress.of(info.value(), info.max(), info.description()));
            if (info.isComplete()) {
                elemental2.dom.DomGlobal.setTimeout(e -> progress.next(Progress.hide()), 2000);
            }
        });
    }
}
