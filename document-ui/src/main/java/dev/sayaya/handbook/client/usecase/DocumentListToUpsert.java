package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 서로 다른 오퍼레이션에 의해 여러 번 add가 호출될 수 있다.
@Singleton
public class DocumentListToUpsert {
    private final Map<String, List<Document>> filter = new ConcurrentHashMap<>();
    private final BehaviorSubject<Set<Document>> subject = behavior(Set.of());
    @Inject DocumentListToUpsert() {}
    public void add(Document document) {
        String key = document.id();
        filter.computeIfAbsent(key, k -> Collections.synchronizedList(new LinkedList<>())).add(document);
        fireSubscribe();
        DomGlobal.console.log(filter);
    }
    public void remove(Document document) {
        String key = document.id();
        filter.computeIfPresent(key, (k, list) -> {
            if (!list.isEmpty()) list.remove(document);
            return list.isEmpty() ? null : list; // list가 비었으면 map에서 항목을 제거 (null 반환)
        });
        fireSubscribe();
    }
    private void fireSubscribe() {
        var distinctChanged = filter.values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableSet());
        if(subject.getValue().size()!=distinctChanged.size()) subject.next(distinctChanged);  // replace 오퍼레이션이 없기 때문에
    }
    public Set<Document> getValue() {
        return subject.getValue();
    }
    public Subscription subscribe(Observer<Set<Document>> var1) {
        return subject.subscribe(var1);
    }
    public Subscription subscribe(Consumer<Set<Document>> consumer) {
        return subject.subscribe(consumer);
    }
    public void clear() {
        filter.clear();
        subject.next(Set.of());
    }
}
