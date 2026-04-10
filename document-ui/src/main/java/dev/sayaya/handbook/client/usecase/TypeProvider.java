package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 현재 선택된 타입 상태. */
@Singleton
public class TypeProvider {
    private final BehaviorSubject<TypeInfo> subject = behavior(null);

    @Inject TypeProvider() {}

    public void next(TypeInfo type) { subject.next(type); }
    public TypeInfo getValue() { return subject.getValue(); }
    public Observable<TypeInfo> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<TypeInfo> consumer) { subject.subscribe(consumer::accept); }
}
