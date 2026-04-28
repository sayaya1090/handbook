package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 선택된 타입 상태 관리자.
 *
 * <p><b>책임:</b> {@link dev.sayaya.rx.subject.BehaviorSubject} 기반으로 사용자가 선택한
 * 현재 타입을 반응형으로 관리한다. 타입이 변경되면 문서 목록 조회, 컬럼 재구성 등
 * 연쇄 동작이 트리거된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.rx.subject.BehaviorSubject} — 최신 값 캐싱과 구독 관리</li>
 *   <li>{@link dev.sayaya.handbook.domain.TypeValue} — 관리 대상 타입 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 초기값은 null이다. 타입이 선택되기 전까지 구독자는 null을 수신한다.</p>
 */
@Singleton
public class TypeProvider {
    @Delegate private final BehaviorSubject<TypeValue> _this = behavior(null);
    @Inject TypeProvider() {}
}
