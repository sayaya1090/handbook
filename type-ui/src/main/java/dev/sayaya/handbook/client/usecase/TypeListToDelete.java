package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 삭제는 여러 번 수행될수가 없다
@Singleton
public class TypeListToDelete {
    private final RepositoryTypeCache repository;
    private final Map<String, Type> filter = new ConcurrentHashMap<>();
    private final BehaviorSubject<Set<Type>> types = behavior(Set.of());
    @Inject TypeListToDelete(RepositoryTypeCache repository) {
        this.repository = repository;
    }
    public void add(Type type) {
        if (repository.contains(type) && filter.putIfAbsent(key(type), type) == null) types.next(Set.copyOf(filter.values()));
    }
    public void remove(Type type) {
        if (filter.remove(key(type)) != null) types.next(Set.copyOf(filter.values()));
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
}
