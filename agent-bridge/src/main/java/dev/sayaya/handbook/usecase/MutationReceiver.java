package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;

/**
 * 에이전트 mutation 이벤트 수신 포트.
 * agent-ui가 MutateCommand를 수신하면 이 인터페이스를 통해 발행하고,
 * type-ui 등 편집 모듈이 구독하여 Action으로 변환한다.
 */
public interface MutationReceiver {
    Observable<String[]> mutations();
}
