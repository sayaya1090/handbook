package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.rx.Observable;

public interface WorkspaceRepository {
    Observable<String> create(String name);
    Observable<String> join(String id);
}
