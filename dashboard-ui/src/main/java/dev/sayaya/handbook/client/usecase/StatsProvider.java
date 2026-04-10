package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.WorkspaceStats;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 워크스페이스 통계의 반응형 상태 홀더.
 *
 * <p><b>책임:</b> BehaviorSubject로 WorkspaceStats를 관리하고, UI 컴포넌트가 구독할 수 있는 Observable을 제공한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link BehaviorSubject} — 반응형 상태 관리</li></ul></p>
 */
@Singleton
public class StatsProvider {
    private final BehaviorSubject<WorkspaceStats> subject = behavior(null);

    @Inject StatsProvider() {}

    public void next(WorkspaceStats stats) { subject.next(stats); }
    public WorkspaceStats getValue() { return subject.getValue(); }
    public Observable<WorkspaceStats> asObservable() { return subject.asObservable(); }
    public void subscribe(java.util.function.Consumer<WorkspaceStats> consumer) { subject.subscribe(consumer::accept); }
}
