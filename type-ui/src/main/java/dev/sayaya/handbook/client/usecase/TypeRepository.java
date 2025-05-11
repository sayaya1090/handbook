package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;

import java.util.List;
import java.util.Set;

public interface TypeRepository {
    Observable<List<Type>> list(Period period);
    Observable<Void> save(Set<Type> toDelete, Set<Type> toUpsert);
}
