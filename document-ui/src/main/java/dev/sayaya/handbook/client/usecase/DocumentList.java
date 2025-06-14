package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class DocumentList {
    private final List<Document> container = new LinkedList<>();
    private final BehaviorSubject<List<Document>> subject = behavior(container);
    @Inject DocumentList() {}
    public void set(Document... documents) {
        container.clear();
        add(documents);
    }
    public void add(Document... documents) {
        Arrays.stream(documents).filter(Objects::nonNull).forEach(container::add);
        this.subject.next(container.stream().collect(Collectors.toUnmodifiableList()));
    }
    public void remove(Document... documents) {
        for(var document: documents) container.remove(document);
        this.subject.next(container.stream().collect(Collectors.toUnmodifiableList()));
    }
    public void replace(Document before, Document after) {
        for(int i = 0; i < container.size(); i++) {
            if(container.get(i).id().equals(before.id())) {
                container.remove(i);
                container.add(i, after);
                break;
            }
        }
        this.subject.next(container.stream().collect(Collectors.toList()));
    }
    public void replaces(Map<Document, Document> beforeAfter) {
        var map = new HashMap<String, Document>();
        for(var entry: beforeAfter.entrySet()) map.put(entry.getKey().id(), entry.getValue());
        for(int i = 0; i < container.size(); i++) {
            String id = container.get(i).id();
            if(map.containsKey(id)) {
                container.remove(i);
                container.add(i, map.get(id));
                map.remove(id);
                if(map.isEmpty()) break;
            }
        }
        this.subject.next(container.stream().collect(Collectors.toList()));
    }
    public Observable<List<Document>> asObservable() {
        return subject.asObservable();
    }
    public <T> Observable<T> map(Function<List<Document>, T> mapper) {
        return subject.map(mapper);
    }
    public Subscription subscribe(Consumer<List<Document>> consumer) {
        return subject.subscribe(consumer);
    }
    public List<Document> getValue() {
        return subject.getValue();
    }
}
