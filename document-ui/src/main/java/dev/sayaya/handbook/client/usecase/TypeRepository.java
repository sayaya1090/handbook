package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;

import java.util.List;

public interface TypeRepository {
    Observable<List<Type>> list();
}
