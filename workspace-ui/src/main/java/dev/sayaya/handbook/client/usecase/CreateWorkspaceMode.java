package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 워크스페이스 생성 모드: CREATE(새로 만들기) / JOIN(기존 참여). */
@Singleton
public class CreateWorkspaceMode {
    public enum Mode { CREATE, JOIN }

    private final BehaviorSubject<Mode> subject = behavior(Mode.CREATE);

    @Inject CreateWorkspaceMode() {}

    public Mode getValue() { return subject.getValue(); }
    public void next(Mode mode) { subject.next(mode); }
    public void subscribe(Consumer<Mode> consumer) { subject.subscribe(consumer::accept); }
}
