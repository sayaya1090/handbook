package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 워크스페이스 생성 파라미터(이름 또는 ID). */
@Singleton
public class CreateWorkspaceParam {
    private final BehaviorSubject<String> subject = behavior(null);

    @Inject CreateWorkspaceParam() {}

    public String getValue() { return subject.getValue(); }
    public void next(String value) { subject.next(value); }
    public void subscribe(Consumer<String> consumer) { subject.subscribe(consumer::accept); }
}
