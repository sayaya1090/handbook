package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class TypeList {
    @Delegate private final BehaviorSubject<Map<String, Map<String, Type>>> subject = behavior(Map.of());
    @Inject TypeList() {}
}