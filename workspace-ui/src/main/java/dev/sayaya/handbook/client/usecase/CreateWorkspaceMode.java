package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 워크스페이스 생성 모드: CREATE(새로 만들기) / JOIN(기존 참여). */
@Singleton
public class CreateWorkspaceMode {
    public enum Mode { CREATE, JOIN }

    @Delegate private final BehaviorSubject<Mode> _this = behavior(Mode.CREATE);
    @Inject CreateWorkspaceMode() {}
}
