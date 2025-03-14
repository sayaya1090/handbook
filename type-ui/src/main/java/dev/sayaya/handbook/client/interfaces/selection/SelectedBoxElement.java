package dev.sayaya.handbook.client.interfaces.selection;

import dev.sayaya.handbook.client.interfaces.box.BoxElement;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class SelectedBoxElement {
    @Delegate private final BehaviorSubject<Set<BoxElement>> elements = behavior(Set.of());
    @Inject SelectedBoxElement() {}
}
