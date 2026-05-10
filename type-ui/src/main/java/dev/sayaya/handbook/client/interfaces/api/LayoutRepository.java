package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.rx.Observable;

import java.util.List;
import java.util.Map;

public interface LayoutRepository {
    Observable<List<TypeLayout>> layouts();
    Observable<Map<String, Position>> positions(LayoutPeriod period);
    Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions);
}
