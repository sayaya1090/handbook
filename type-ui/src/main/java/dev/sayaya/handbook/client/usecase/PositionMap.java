package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 타입별 캔버스 위치. TypeLayout.positions와 대응. */
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

    public void subscribe(Consumer<Map<String, Position>> consumer) {
        subject.subscribe(consumer::accept);
    }
}
