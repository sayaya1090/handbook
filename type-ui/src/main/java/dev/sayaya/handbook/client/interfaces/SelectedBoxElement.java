package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class SelectedBoxElement {
    @Delegate private final BehaviorSubject<BoxElement> elements = behavior(null);
    @Inject SelectedBoxElement() {}
}
