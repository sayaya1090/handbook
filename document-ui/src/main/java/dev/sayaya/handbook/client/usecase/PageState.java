package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 검색 조건 및 페이지네이션 상태 관리자.
 *
 * <p><b>책임:</b> {@link dev.sayaya.rx.subject.BehaviorSubject} 기반으로 현재 검색 조건과
 * 페이지 번호를 반응형으로 관리한다. {@link dev.sayaya.handbook.client.interfaces.table.PaginationElement}에서
 * 페이지 변경 시 이 상태를 갱신하며, 문서 조회 로직이 이를 구독하여 API 호출을 트리거한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.rx.subject.BehaviorSubject} — 최신 값 캐싱과 구독 관리</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.Search} — 검색 조건 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 초기값은 Search.defaultSearch()이다. 페이지 번호는 0-base이다.</p>
 */
@Singleton
public class PageState {
    @Delegate private final BehaviorSubject<Search> _this = behavior(Search.defaultSearch());
    @Inject PageState() {}
}
