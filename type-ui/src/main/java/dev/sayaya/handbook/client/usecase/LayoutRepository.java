package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.rx.Observable;

import java.util.List;

public interface LayoutRepository {
    Observable<List<Period>> layouts();
}
