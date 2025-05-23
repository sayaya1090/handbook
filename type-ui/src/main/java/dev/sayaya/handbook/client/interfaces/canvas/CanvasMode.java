package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class CanvasMode {
    @Delegate private final BehaviorSubject<CanvasState> elements = behavior(CanvasState.VIEW);
    @Inject CanvasMode() {}
}
