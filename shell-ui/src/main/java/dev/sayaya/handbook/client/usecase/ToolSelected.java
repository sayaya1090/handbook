package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class ToolSelected {
    @Delegate private final BehaviorSubject<Tool> _this = behavior(null);
    @Inject ToolSelected(ToolExecutionManager executor, dev.sayaya.handbook.usecase.ToolProvider toolProvider) {
        _this.distinctUntilChanged().subscribe(tool -> {
            executor.register(tool);
            if(tool != null && tool.id() != null) toolProvider.select(tool.id());
        });
    }
}
