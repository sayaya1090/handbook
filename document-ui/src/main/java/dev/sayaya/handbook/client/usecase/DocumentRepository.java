package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.rx.Observable;

import java.util.Set;

public interface DocumentRepository {
    Observable<Void> save(Set<Document> toUpsert);
    Observable<Void> delete(Set<Document> toDelete);
}
