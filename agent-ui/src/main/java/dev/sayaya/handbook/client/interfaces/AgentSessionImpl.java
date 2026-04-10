package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.AgentSessionState;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

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
