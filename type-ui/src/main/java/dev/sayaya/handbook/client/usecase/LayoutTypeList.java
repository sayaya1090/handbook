package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class LayoutTypeList {
    private final Map<Period, List<String>> typeIdCache = new HashMap<>();
    private final Map<String, Type> typeCache = new HashMap<>();
    private final LayoutProvider layoutProvider;
    private final RepositoryTypeCache repository;
    private final TypeListEditing typeListEditing;
    private final TypeListToDelete typeListToDelete;
    @Inject LayoutTypeList(LayoutProvider layoutProvider, RepositoryTypeCache repository, TypeListEditing typeListEditing, TypeListToDelete typeListToDelete) {
        this.layoutProvider = layoutProvider;
        this.repository = repository;
        this.typeListEditing = typeListEditing;
        this.typeListToDelete = typeListToDelete;
    }
    public void initialize() {
        layoutProvider.subscribe(this::update);
    }
    void update(Period period) {
        if (period != null) {
            if(!typeIdCache.containsKey(period)) repository.list(period).subscribe(types->{
                types.forEach(type-> typeCache.putIfAbsent(key(type), type));
                typeIdCache.computeIfAbsent(period, k->new LinkedList<>())
                        .addAll(types.stream().map(LayoutTypeList::key).collect(Collectors.toList()));
                fireSubscribe(period);
            }); else fireSubscribe(period);
        }
    }
    private void fireSubscribe(Period period) {
        var deleteCandidates = typeListToDelete.getValue();
        var typesToEdit = typeIdCache.get(period).stream()
                .map(typeCache::get)
                .filter(Objects::nonNull)
                .filter(type -> !deleteCandidates.contains(type))
                .toArray(Type[]::new);
        typeListEditing.next(typesToEdit);
    }
    private static String key(Type type) {
        return type.id() + "$$$" + type.version();
    }
}

