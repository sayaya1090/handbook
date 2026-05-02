package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Document;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 타입의 문서 목록 상태 관리자.
 *
 * <p><b>책임:</b> {@link dev.sayaya.rx.subject.BehaviorSubject} 기반으로 현재 선택된 타입의
 * 문서 목록을 반응형으로 관리한다. 문서 추가/삭제/편집 액션과 API 응답 모두
 * 이 클래스를 통해 상태를 갱신하며, 구독자에게 변경을 전파한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.rx.subject.BehaviorSubject} — 최신 값 캐싱과 구독 관리</li>
 *   <li>{@link dev.sayaya.handbook.domain.Document} — 관리 대상 문서 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 초기값은 빈 리스트(Collections.emptyList())이다.
 * 상태를 갱신할 때는 불변 리스트가 아닌 새 ArrayList를 전달해야 한다.</p>
 */
@Singleton
public class DocumentList {
    @Delegate private final BehaviorSubject<List<Document>> _this = behavior(Collections.emptyList());
    @Inject DocumentList() {}
}
