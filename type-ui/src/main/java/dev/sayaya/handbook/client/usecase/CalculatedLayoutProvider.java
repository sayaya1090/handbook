package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class CalculatedLayoutProvider {
    @Delegate private final BehaviorSubject<List<Period>> subject = behavior(List.of());
    @Inject CalculatedLayoutProvider() { }
    public Observable<List<Period>> asObservable() { return subject.distinctUntilChanged(); }
}

