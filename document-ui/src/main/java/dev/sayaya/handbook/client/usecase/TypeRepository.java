package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.rx.Observable;

/** 타입 목록 조회 포트 인터페이스. 컬럼 정의를 위해 사용한다. */
public interface TypeRepository {
    Observable<TypeInfo[]> list();
}
