package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.DocumentValue;
import dev.sayaya.rx.Observable;
import jsinterop.base.JsPropertyMap;

import java.util.List;

/**
 * 문서 저장/삭제/조회 포트 인터페이스.
 * 백엔드와 프론트엔드에서 공동으로 사용하는 저장소 계약.
 */
public interface DocumentRepository {
    Observable<DocumentValue[]> search(String type, int page, int limit);
    Observable<Void> save(List<DocumentValue> documents);
    /** 변경 필드만 전송하는 패치 저장. */
    Observable<Void> patch(List<JsPropertyMap<?>> patches);
    Observable<Void> delete(List<DocumentValue> documents);
}
