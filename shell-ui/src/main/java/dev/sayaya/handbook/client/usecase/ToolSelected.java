package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class ToolSelected {
    @Delegate private final BehaviorSubject<Tool> _this = behavior(null);
    @Inject ToolSelected(ToolExecutionManager executor) {
        _this.distinctUntilChanged().subscribe(executor::register);
    }
}
