package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.rx.Observable;

import java.util.List;
import java.util.Map;

/** 레이아웃 API 포트. */
public interface LayoutRepository {
    Observable<List<LayoutPeriod>> layouts();
    Observable<Map<String, Position>> positions(LayoutPeriod period);
    Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions);
}
