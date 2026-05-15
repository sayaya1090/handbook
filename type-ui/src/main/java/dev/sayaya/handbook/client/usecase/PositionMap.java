package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Position;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 타입별 캔버스 위치를 관리하는 반응형 상태 컨테이너.
 *
 * <p><b>책임:</b> {@link BehaviorSubject}를 통해 타입 키 → {@link Position} 매핑을 관리하며,
 * 위치 조회(get), 설정(put), 이동(move), 전체 교체(replace) 연산과
 * 구독(subscribe) 기능을 제공한다. 백엔드 TypeLayout.positions와 대응한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BehaviorSubject} — 반응형 상태 관리</li>
 *   <li>{@link Position} — 보유하는 위치 값 객체</li>
 * </ul></p>
 * <p><b>주의:</b> 키는 Type.key() 형식("id:version")이다. 모든 변경은 새 Map 인스턴스를 생성한다.</p>
 */
@Singleton
public class PositionMap {
    private final BehaviorSubject<Map<String, Position>> subject = behavior(Collections.emptyMap());

    @Inject PositionMap() {}

    public Observable<Map<String, Position>> observable() {
        return subject.asObservable();
    }

    public Map<String, Position> getValue() {
        return subject.getValue();
    }

    public void replace(Map<String, Position> positions) {
        subject.next(positions);
    }

    public Position get(String typeKey) {
        return subject.getValue().get(typeKey);
    }

    public void put(String typeKey, Position position) {
        Map<String, Position> next = new LinkedHashMap<>(subject.getValue());
        next.put(typeKey, position);
        subject.next(next);
    }

    public void move(String typeKey, int dx, int dy) {
        Position current = get(typeKey);
        if (current == null) return;
        put(typeKey, current.move(dx, dy));
    }

    public void changeKey(String oldKey, String newKey) {
        Map<String, Position> next = new LinkedHashMap<>(subject.getValue());
        Position pos = next.remove(oldKey);
        if (pos != null) {
            next.put(newKey, pos);
            subject.next(next);
        }
    }

    public void subscribe(Consumer<Map<String, Position>> consumer) {
        subject.subscribe(consumer::accept);
    }
}
