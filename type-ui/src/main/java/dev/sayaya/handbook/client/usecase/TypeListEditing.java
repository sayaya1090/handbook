package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 현재 다루고 있는 Type 목록
@Singleton
public class TypeListEditing {
    @Delegate private final BehaviorSubject<Type[]> types = behavior(new Type[0]);
    @Inject TypeListEditing() {}
}
