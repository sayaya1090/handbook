package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 전체 Type 목록
@Singleton
public class TypeList {
    private final Set<Period> visited = new HashSet<>();
    private final TypeRepository repository;
    private final Map<String, Type> map = new ConcurrentHashMap<>();
    @Delegate private final BehaviorSubject<Set<Type>> types = behavior(Set.of());
    @Inject TypeList(LayoutProvider layoutProvider, TypeRepository repository) {
        this.repository = repository;
        layoutProvider.distinctUntilChanged().subscribe(this::initialize);
    }
    private void initialize(Period period) {
        if(period == null) return;
        if(!visited.contains(period)) repository.list(period).subscribe(typesAll->{
            typesAll.stream().filter(Objects::nonNull)
                    .filter(type->!map.containsKey(type.id()))
                    .forEach(type->map.put(type.id(), type));
            visited.add(period);
            publish();
        });
    }
    public void add(Type... types) {
        Arrays.stream(types).filter(Objects::nonNull) .forEach(type->map.put(type.id(), type));
        publish();
    }
    public void remove(Type... types) {
        for(var type: types) map.remove(type.id());
        publish();
    }
    public void replace(Type before, Type after) {
        map.put(before.id(), after);
        publish();
    }
    public void reset() {
        visited.clear();
    }
    private void publish() {
        types.next(map.values().stream().collect(Collectors.toUnmodifiableSet()));
    }
}
