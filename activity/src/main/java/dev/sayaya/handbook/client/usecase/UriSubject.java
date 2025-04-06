package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import static elemental2.dom.DomGlobal.window;

public class UriSubject {
    @Delegate private final BehaviorSubject<String> subject = BehaviorSubject.behavior(window.location.href);
}
