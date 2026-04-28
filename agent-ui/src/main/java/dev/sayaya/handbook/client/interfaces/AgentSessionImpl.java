package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.domain.AgentSessionState;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * AgentSession의 BehaviorSubject 기반 구현체.
 *
 * <p><b>책임:</b> BehaviorSubject로 에이전트 세션 상태를 관리하고, Observable/Observer 인터페이스를 제공한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link BehaviorSubject} — 반응형 상태 관리</li></ul></p>
 */
@Singleton
public class AgentSessionImpl implements AgentSession {
    private final BehaviorSubject<AgentSessionState> subject = behavior(AgentSessionState.IDLE);

    @Inject
    public AgentSessionImpl() {}

    @Override
    public Observable<AgentSessionState> state() { return subject; }

    @Override
    public Observer<AgentSessionState> stateObserver() { return subject; }
}
