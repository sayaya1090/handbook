package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 전체 타입 목록 상태 관리자.
 *
 * <p><b>책임:</b> {@link dev.sayaya.rx.subject.BehaviorSubject} 기반으로 워크스페이스에서
 * 사용 가능한 타입 목록을 반응형으로 관리한다. 타입 API 응답을 수신하여 상태를 갱신하고,
 * {@link dev.sayaya.handbook.client.interfaces.controller.TypeTabsElement} 등 구독자에게 변경을 전파한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.rx.subject.BehaviorSubject} — 최신 값 캐싱과 구독 관리</li>
 *   <li>{@link dev.sayaya.handbook.domain.TypeValue} — 관리 대상 타입 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 초기값은 빈 리스트(Collections.emptyList())이다.</p>
 */
@Singleton
public class TypeList {
    @Delegate private final BehaviorSubject<List<TypeValue>> _this = behavior(Collections.emptyList());
    @Inject TypeList() {}
}
