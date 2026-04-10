package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 현재 타입의 문서 목록 상태. */
@Singleton
public class DocumentList {
    private final BehaviorSubject<List<DocumentValue>> subject = behavior(Collections.emptyList());

    @Inject DocumentList() {}

    public void next(List<DocumentValue> docs) { subject.next(docs); }
    public List<DocumentValue> getValue() { return subject.getValue(); }
    public Observable<List<DocumentValue>> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<List<DocumentValue>> consumer) { subject.subscribe(consumer::accept); }
}
