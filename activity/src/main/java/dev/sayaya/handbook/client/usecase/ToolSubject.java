package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

public class ToolSubject {
    @Delegate final BehaviorSubject<List<Tool>> subject = behavior(List.of());
}
