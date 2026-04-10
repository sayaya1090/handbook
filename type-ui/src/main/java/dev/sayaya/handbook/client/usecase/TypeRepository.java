package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

import java.util.List;
import java.util.Set;

/**
 * 타입 CRUD를 위한 유스케이스 포트 인터페이스(헥사고날 아키텍처).
 *
 * <p><b>책임:</b> 기간별 타입 목록 조회(list), 전체 저장(save), 부분 패치(patch),
 * 삭제(delete) 연산을 정의한다. 구현체는 인프라 계층의 HTTP 어댑터({@link dev.sayaya.handbook.client.interfaces.api.TypeApi}).</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeValue} — 반환/입력 도메인 객체</li>
 *   <li>{@link LayoutPeriod} — 조회 시 기간 파라미터</li>
 *   <li>{@link dev.sayaya.rx.Observable} — 비동기 결과 스트림</li>
 * </ul></p>
 * <p><b>주의:</b> patch()의 각 항목은 {id, version, rev, attributes: [...]} 형태의 JsPropertyMap이다.</p>
 */
public interface TypeRepository {
    Observable<Set<TypeValue>> list(LayoutPeriod period);
    Observable<Set<TypeValue>> save(Set<TypeValue> types);
    /** 변경 속성만 전송하는 패치 저장. 각 항목은 {id, version, rev, attributes: [...]} 형태. */
    Observable<Set<TypeValue>> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(Set<TypeValue> types);
}
