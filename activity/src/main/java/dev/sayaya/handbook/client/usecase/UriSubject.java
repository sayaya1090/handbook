package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsType;

import static elemental2.dom.DomGlobal.window;

@JsType
public class UriSubject {
    final BehaviorSubject<String> subject = BehaviorSubject.behavior(window.location.href);
}
