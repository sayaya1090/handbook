package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.WorkspaceStats;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 워크스페이스 통계 상태. */
@Singleton
public class StatsProvider {
    private final BehaviorSubject<WorkspaceStats> subject = behavior(null);

    @Inject StatsProvider() {}

    public void next(WorkspaceStats stats) { subject.next(stats); }
    public WorkspaceStats getValue() { return subject.getValue(); }
    public Observable<WorkspaceStats> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<WorkspaceStats> consumer) { subject.subscribe(consumer::accept); }
}
