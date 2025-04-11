package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsType;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@JsType
public class ProgressSubject {
    final BehaviorSubject<Progress> subject = behavior(new Progress());
}
