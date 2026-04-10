package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.domain.ToolFunction;
import dev.sayaya.rx.Subscription;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.Observable.timer;

/**
 * 도구 함수 실행을 관리한다.
 * 즉시 실행 후, 완료되지 않으면 100ms마다 재시도한다.
 */
@Singleton
public class ToolExecutionManager {
    private Subscription executionSubscription;
    @Inject ToolExecutionManager() {}
    void register(Tool tool) {
        if(tool == null || tool.function() == null) return;
        if(executionSubscription != null) executionSubscription.unsubscribe();
        executionSubscription = timer(0, 100).subscribe(t -> execute(tool.function()));
    }
    private void execute(ToolFunction function) {
        if(function != null && function.repeat() && executionSubscription != null)
            executionSubscription.unsubscribe();
    }
}
