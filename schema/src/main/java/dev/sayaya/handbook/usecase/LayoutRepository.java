package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Position;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

/**
 * 타입 레이아웃 저장소 인터페이스.
 */
public interface LayoutRepository {
    Observable<JsPropertyMap<Position>> load(String workspaceId);
    Observable<Void> save(String workspaceId, JsPropertyMap<Position> positions);
}
