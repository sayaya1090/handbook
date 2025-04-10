package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

public class ProgressSubject {
    @Delegate final BehaviorSubject<Progress> subject = behavior(new Progress());
}
