package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 전체 타입 목록 상태. */
@Singleton
public class TypeList {
    private final BehaviorSubject<List<TypeInfo>> subject = behavior(Collections.emptyList());

    @Inject TypeList() {}

    public void next(List<TypeInfo> types) { subject.next(types); }
    public List<TypeInfo> getValue() { return subject.getValue(); }
    public Observable<List<TypeInfo>> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<List<TypeInfo>> consumer) { subject.subscribe(consumer::accept); }
}
