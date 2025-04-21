package dev.sayaya.handbook.client.usecase.create;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class CreateWorkspaceMode {
    @Delegate private final BehaviorSubject<CreateWorkspaceState> _this = behavior(CreateWorkspaceState.CREATE);
    @Inject CreateWorkspaceMode() {}
}
