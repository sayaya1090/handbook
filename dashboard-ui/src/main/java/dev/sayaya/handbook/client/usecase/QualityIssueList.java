package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 품질 이슈 목록 상태. */
@Singleton
public class QualityIssueList {
    private final BehaviorSubject<List<QualityIssue>> subject = behavior(Collections.emptyList());

    @Inject QualityIssueList() {}

    public void next(List<QualityIssue> issues) { subject.next(issues); }
    public List<QualityIssue> getValue() { return subject.getValue(); }
    public Observable<List<QualityIssue>> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<List<QualityIssue>> consumer) { subject.subscribe(consumer::accept); }
}
