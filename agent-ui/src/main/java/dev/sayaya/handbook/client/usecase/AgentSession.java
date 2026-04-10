package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.AgentSessionState;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

/**
 * 에이전트 세션 상태를 관리한다.
 * Shell이 세션 상태를 관찰하여 UI를 갱신한다.
 */
public interface AgentSession {
    Observable<AgentSessionState> state();
    Observer<AgentSessionState> stateObserver();
}
