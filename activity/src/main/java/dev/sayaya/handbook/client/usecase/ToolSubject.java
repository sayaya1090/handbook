package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.annotations.JsType;

import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@JsType
public class ToolSubject {
    final BehaviorSubject<List<Tool>> subject = behavior(List.of());
}
