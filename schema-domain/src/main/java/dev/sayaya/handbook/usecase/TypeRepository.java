package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

import java.util.List;
import java.util.Set;

/**
 * 타입 정보를 관리하는 저장소 인터페이스.
 */
public interface TypeRepository {
    Observable<Set<TypeValue>> list(LayoutPeriod period);
    Observable<Set<TypeValue>> save(Set<TypeValue> types);
    Observable<Set<TypeValue>> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(Set<TypeValue> types);
    Observable<Set<TypeValue>> versions(String typeId);
}
