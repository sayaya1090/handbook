package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 삭제는 여러 번 수행될수가 없다
@Singleton
public class DocumentListToDelete {
    private final DocumentList documents;
    private final BehaviorSubject<Set<Document>> subject = behavior(Set.of());
    @Inject DocumentListToDelete(DocumentList documents) {
        this.documents = documents;
        documents.subscribe(this::filter);
    }
    private void filter(List<Document> documents) {
        var next = documents.stream().filter(doc->doc.state() == Document.DocumentState.DELETE).collect(Collectors.toSet());
        subject.next(next);
    }
    public void refresh() {
        filter(documents.getValue());
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
}
