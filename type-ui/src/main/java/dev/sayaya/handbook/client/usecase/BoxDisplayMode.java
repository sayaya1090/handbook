package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class BoxDisplayMode {
    @Delegate private final BehaviorSubject<dev.sayaya.handbook.client.domain.BoxDisplayMode> elements = behavior(dev.sayaya.handbook.client.domain.BoxDisplayMode.SIMPLE);
    @Inject BoxDisplayMode() {}
}
