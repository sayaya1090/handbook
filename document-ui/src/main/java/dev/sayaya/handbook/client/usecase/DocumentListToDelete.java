package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
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
public class DocumentListToDelete {
    private final Map<String, Document> filter = new ConcurrentHashMap<>();
    private final BehaviorSubject<Set<Document>> subject = behavior(Set.of());
    @Inject DocumentListToDelete() {}
    public void add(Document type) {
        if (filter.putIfAbsent(type.id(), type) == null) subject.next(Set.copyOf(filter.values()));
    }
    public void remove(Document type) {
        if (filter.remove(type.id()) != null) subject.next(Set.copyOf(filter.values()));
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
