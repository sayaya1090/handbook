package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

import java.util.List;
import java.util.Set;

/** 타입 API 포트. */
public interface TypeRepository {
    Observable<Set<TypeValue>> list(LayoutPeriod period);
    Observable<Set<TypeValue>> save(Set<TypeValue> types);
    /** 변경 속성만 전송하는 패치 저장. 각 항목은 {id, version, rev, attributes: [...]} 형태. */
    Observable<Set<TypeValue>> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(Set<TypeValue> types);
}
