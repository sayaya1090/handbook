package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.ProgressInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * agent-ui의 progress 커맨드를 Shell의 공용 프로그레스 바(Observer&lt;Progress&gt;)로 위임한다.
 * 완료 시 자동으로 프로그레스 바를 숨긴다.
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
