package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 서로 다른 오퍼레이션에 의해 여러 번 add가 호출될 수 있다.
@Singleton
public class TypeListToUpsert {
    private final RepositoryTypeCache repository;
    private final Map<String, List<Type>> filter = new ConcurrentHashMap<>();
    private final BehaviorSubject<Set<Type>> types = behavior(Set.of());
    @Inject TypeListToUpsert(RepositoryTypeCache repository) {
        this.repository = repository;
    }
    public void add(Type type) {
        String key = key(type);
        filter.computeIfAbsent(key, k -> Collections.synchronizedList(new LinkedList<>())).add(type);
        fireSubscribe();
    }
    public void remove(Type type) {
        String key = key(type);
        filter.computeIfPresent(key, (k, list) -> {
            if (!list.isEmpty()) list.remove(type);
            return list.isEmpty() ? null : list; // list가 비었으면 map에서 항목을 제거 (null 반환)
        });
        fireSubscribe();
    }
    private void fireSubscribe() {
        var distinctChanged = filter.values().stream().flatMap(List::stream).filter(repository::isChanged).collect(Collectors.toUnmodifiableSet());
        if(types.getValue().size()!=distinctChanged.size()) types.next(distinctChanged);  // replace 오퍼레이션이 없기 때문에
    }
    private static String key(Type type) {
        return type.id() + "$$$" + type.version();
    }
    public Set<Type> getValue() {
        return types.getValue();
    }
    public Subscription subscribe(Observer<Set<Type>> var1) {
        return types.subscribe(var1);
    }
    public Subscription subscribe(Consumer<Set<Type>> consumer) {
        return types.subscribe(consumer);
    }
    public void clear() {
        filter.clear();
        types.next(Set.of());
    }
}
