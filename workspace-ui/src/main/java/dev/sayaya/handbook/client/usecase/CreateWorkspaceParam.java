package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 워크스페이스 생성 파라미터(이름 또는 ID). */
@Singleton
public class CreateWorkspaceParam {
    @Delegate private final BehaviorSubject<String> _this = behavior(null);
    @Inject CreateWorkspaceParam() {}
}
