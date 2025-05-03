package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.rx.Observable;

import java.util.List;

public interface TypeRepository {
    Observable<List<Box>> list(Period period);
    Observable<Void> save(List<Box> boxes);
}
