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

// 전체 Type 목록
@Singleton
public class TypeList {
    private final Set<Period> visited = new HashSet<>();
    private final RepositoryTypeCache repository;
    private final TypeListToDelete typeListToDelete;
    private final Set<Type> container = new HashSet<>();
    @Delegate private final BehaviorSubject<Set<Type>> types = behavior(container.stream().collect(Collectors.toUnmodifiableSet()));
    @Inject TypeList(LayoutProvider layoutProvider, RepositoryTypeCache repository, TypeListToDelete typeListToDelete) {
        this.repository = repository;
        this.typeListToDelete = typeListToDelete;
        layoutProvider.distinctUntilChanged().subscribe(this::initialize);
    }
    private void initialize(Period period) {
        if(period == null) return;
        var deleteCandidates = typeListToDelete.getValue();
        if(!visited.contains(period)) repository.list(period).subscribe(typesAll->{
            typesAll.stream().filter(Objects::nonNull)
                    .filter(type -> !deleteCandidates.contains(type))
                    .forEach(container::add);
            visited.add(period);
            next(container.stream().collect(Collectors.toUnmodifiableSet()));
        });
    }
    public void add(Type... types) {
        var deleteCandidates = typeListToDelete.getValue();
        Arrays.stream(types).filter(Objects::nonNull)
                .filter(type -> !deleteCandidates.contains(type))
                .forEach(container::add);
        this.types.next(container.stream().collect(Collectors.toUnmodifiableSet()));
    }
    public void remove(Type... types) {
        for(var type: types) container.remove(type);
        this.types.next(container.stream().collect(Collectors.toUnmodifiableSet()));
    }
    public void replace(Type before, Type after) {
        container.remove(before);
        var deleteCandidates = typeListToDelete.getValue();
        if(!deleteCandidates.contains(after)) container.add(after);
        types.next(container.stream().collect(Collectors.toUnmodifiableSet()));
    }
    public void reset() {
        visited.clear();
    }
}
