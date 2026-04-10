package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;

/** 워크스페이스 API 포트. */
public interface WorkspaceRepository {
    Observable<String> create(String name, String description);
    Observable<String> update(String id, String name, String description);
    Observable<Void> delete(String id);
}
