package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.AgentSessionState;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

/**
 * 에이전트 세션 상태 관리 포트 인터페이스.
 *
 * <p><b>책임:</b> 에이전트 세션 상태의 Observable(읽기)과 Observer(쓰기)를 제공하여 상태 전이를 관리한다.</p>
 * <p><b>의존관계:</b> <ul><li>interfaces 계층의 {@link dev.sayaya.handbook.client.interfaces.AgentSessionImpl}이 구현한다.</li></ul></p>
 */
public interface AgentSession {
    Observable<AgentSessionState> state();
    Observer<AgentSessionState> stateObserver();
}
