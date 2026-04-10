package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 현재 로딩된 전체 타입 목록. */
@Singleton
public class TypeList {
    private final BehaviorSubject<Set<TypeValue>> subject = behavior(Collections.emptySet());

    @Inject TypeList() {}

    public Observable<Set<TypeValue>> observable() {
        return subject.asObservable();
    }

    public Set<TypeValue> getValue() {
        return subject.getValue();
    }

    public void replace(Set<TypeValue> types) {
        subject.next(types);
    }

    public void add(TypeValue type) {
        Set<TypeValue> next = new LinkedHashSet<>(subject.getValue());
        next.add(type);
        subject.next(next);
    }

    public void remove(TypeValue type) {
        Set<TypeValue> next = new LinkedHashSet<>(subject.getValue());
        next.removeIf(t -> t.key().equals(type.key()));
        subject.next(next);
    }

    public void update(TypeValue before, TypeValue after) {
        Set<TypeValue> next = new LinkedHashSet<>();
        for (TypeValue t : subject.getValue()) {
            if (t.key().equals(before.key())) next.add(after);
            else next.add(t);
        }
        subject.next(next);
    }

    public void subscribe(Consumer<Set<TypeValue>> consumer) {
        subject.subscribe(consumer::accept);
    }
}
