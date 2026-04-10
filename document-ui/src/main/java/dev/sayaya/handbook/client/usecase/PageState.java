package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 검색/페이지네이션 상태. */
@Singleton
public class PageState {
    private final BehaviorSubject<Search> subject = behavior(Search.defaultSearch());

    @Inject PageState() {}

    public void next(Search search) { subject.next(search); }
    public Search getValue() { return subject.getValue(); }
    public Observable<Search> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<Search> consumer) { subject.subscribe(consumer::accept); }
}
