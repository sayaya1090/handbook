package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.DocumentValue;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

import java.util.List;

/** 문서 저장/삭제/조회 포트 인터페이스. */
public interface DocumentRepository {
    Observable<DocumentValue[]> search(String type, int page, int limit);
    Observable<Void> save(List<DocumentValue> documents);
    /** 변경 필드만 전송하는 패치 저장. 각 항목은 {id, rev, data: {변경필드만}} 형태. */
    Observable<Void> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(List<DocumentValue> documents);
}
