package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.rx.Observable;

import java.util.List;

/** 문서 저장/삭제/조회 포트 인터페이스. */
public interface DocumentRepository {
    Observable<DocumentValue[]> search(String type, int page, int limit);
    Observable<Void> save(List<DocumentValue> documents);
    Observable<Void> delete(List<DocumentValue> documents);
}
