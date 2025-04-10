package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.Subscription;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.Observable.timer;

@Singleton
public class ToolExecutionManager {
    private Subscription executionSubscription;
    @Inject ToolExecutionManager() {}
    void register(Tool tool) {
        if(tool==null || tool.function()==null) return;
        if(executionSubscription != null) executionSubscription.unsubscribe();
        executionSubscription = timer(0, 100).subscribe(t->execute(tool.function())); // 즉시 실행 후 미완 시 100밀리초마다 실행
    }
    private void execute(ToolFunction function) {
        if(function!=null && function.repeat() && executionSubscription!=null) executionSubscription.unsubscribe();
    }
}
