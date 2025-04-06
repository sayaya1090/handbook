package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

public class RendererSubject {
    @Delegate final BehaviorSubject<Render> subject = behavior(null);
}
