package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class LayoutTypeList {
    private final TypeList all;
    private final LayoutProvider layoutProvider;
    @Delegate private final BehaviorSubject<Set<Type>> types = behavior(Set.of());
    @Inject LayoutTypeList(LayoutProvider layoutProvider, TypeList all) {
        this.layoutProvider = layoutProvider;
        this.all = all;
        layoutProvider.distinctUntilChanged().subscribe(this::update);
        all.distinctUntilChanged().subscribe(this::update);
    }
    private void update(Period period) {
        next(filter(all.getValue(), period));
    }
    private void update(Set<Type> all) {
        next(filter(all, layoutProvider.getValue()));
    }
    private static Set<Type> filter(Set<Type> all, Period period) {
        if(period == null) return Set.of();
        if(all == null || all.isEmpty()) return Set.of();
        return all.stream().filter(type->contains(type, period)).collect(Collectors.toSet());
    }
    private static boolean contains(Type type, Period period) {
        return  type.effectDateTime() != null && type.expireDateTime() != null && // null 체크 추가
                type.effectDateTime().before(period.expireDateTime()) &&
                type.expireDateTime().after(period.effectDateTime());
    }
}

