package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class WorkspaceProvider {
    @Delegate private final BehaviorSubject<Workspace> _this = behavior(null);
    @Inject WorkspaceProvider() {}
}
