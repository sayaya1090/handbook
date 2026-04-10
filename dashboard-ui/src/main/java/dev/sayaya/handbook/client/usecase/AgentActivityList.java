package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 에이전트 활동 목록 상태. */
@Singleton
public class AgentActivityList {
    private final BehaviorSubject<List<AgentActivity>> subject = behavior(Collections.emptyList());

    @Inject AgentActivityList() {}

    public void next(List<AgentActivity> activities) { subject.next(activities); }
    public List<AgentActivity> getValue() { return subject.getValue(); }
    public Observable<List<AgentActivity>> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<List<AgentActivity>> consumer) { subject.subscribe(consumer::accept); }
}
