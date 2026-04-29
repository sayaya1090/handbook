package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;

public interface AuthRepository {
    Observable<Boolean> refresh();
}
