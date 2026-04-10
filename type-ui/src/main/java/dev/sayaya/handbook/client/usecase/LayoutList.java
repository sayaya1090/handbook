package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 전체 레이아웃 기간 목록. 서버에서 조회하여 갱신한다. */
@Singleton
public class LayoutList {
    private final BehaviorSubject<List<LayoutPeriod>> subject = behavior(Collections.emptyList());

    @Inject LayoutList() {}

    public Observable<List<LayoutPeriod>> observable() {
        return subject.asObservable();
    }

    public List<LayoutPeriod> getValue() {
        return subject.getValue();
    }

    public void next(List<LayoutPeriod> periods) {
        subject.next(periods);
    }

    public void subscribe(Consumer<List<LayoutPeriod>> consumer) {
        subject.subscribe(consumer::accept);
    }
}
