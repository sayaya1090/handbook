package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * 레이아웃 조회/저장을 위한 유스케이스 포트 인터페이스(헥사고날 아키텍처).
 *
 * <p><b>책임:</b> 레이아웃 기간 목록 조회(layouts), 기간별 타입 위치 조회(positions),
 * 위치 저장(savePositions) 연산을 정의한다.
 * 구현체는 인프라 계층의 HTTP 어댑터({@link dev.sayaya.handbook.client.interfaces.api.LayoutApi}).</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LayoutPeriod} — 기간 파라미터/반환 값 객체</li>
 *   <li>{@link Position} — 타입 위치 값 객체</li>
 *   <li>{@link dev.sayaya.rx.Observable} — 비동기 결과 스트림</li>
 * </ul></p>
 * <p><b>주의:</b> positions()의 키는 TypeValue.key() 형식("id:version")이다.</p>
 */
public interface LayoutRepository {
    Observable<List<LayoutPeriod>> layouts();
    Observable<Map<String, Position>> positions(LayoutPeriod period);
    Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions);
}
