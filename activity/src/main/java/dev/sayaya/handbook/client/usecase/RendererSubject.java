package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsType;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@JsType
public class RendererSubject {
    final BehaviorSubject<Render> subject = behavior(null);
}
