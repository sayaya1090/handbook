package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.rx.Observable;

import java.util.Set;

/** 타입 API 포트. */
public interface TypeRepository {
    Observable<Set<TypeValue>> list(LayoutPeriod period);
    Observable<Set<TypeValue>> save(Set<TypeValue> types);
    Observable<Void> delete(Set<TypeValue> types);
}
