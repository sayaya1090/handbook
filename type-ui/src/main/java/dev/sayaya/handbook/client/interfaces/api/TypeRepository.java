package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.rx.Observable;
import java.util.Set;
import java.util.List;
import jsinterop.base.JsPropertyMap;

public interface TypeRepository {
    Observable<Set<Type>> list(LayoutPeriod period);
    Observable<Set<Type>> save(Set<Type> types);
    Observable<Set<Type>> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(Set<Type> types);
    Observable<Set<Type>> versions(String typeId);
}
