package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class DocumentSelectedList {
    private final List<Document> container = new LinkedList<>();
    private final BehaviorSubject<List<Document>> subject = behavior(container);
    @Inject DocumentSelectedList() {}
    public void add(Document document) {
        if(container.contains(document)) return;
        container.add(document);
        subject.next(container);
    }
    public void remove(Document document) {
        if(!container.contains(document)) return;
        container.remove(document);
        subject.next(container);
    }
    public void clear() {
        if(container.isEmpty()) return;
        container.clear();
        subject.next(container);
    }
    public Subscription subscribe(Observer<List<Document>> consumer) {
        return subject.subscribe(consumer);
    }
    public Subscription subscribe(Consumer<List<Document>> consumer) {
        return subject.subscribe(consumer);
    }
    public List<Document> getValue() {
        return container;
    }
    public boolean isEmpty() {
        return container.isEmpty();
    }
}
