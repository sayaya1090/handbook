package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Type;
import dev.sayaya.rx.Observable;

import java.util.List;

/**
 * 타입 스키마 저장소 인터페이스.
 */
public interface TypeRepository {
    Observable<Type[]> search(String query, int page, int limit);
    Observable<Type> load(String id, String version);
    Observable<Void> save(List<Type> types);
}
