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
    private final DocumentList documents;
    private final BehaviorSubject<Set<Document>> subject = behavior(Set.of());
    @Inject DocumentListToUpsert(DocumentList documents) {
        this.documents = documents;
        documents.subscribe(list->{
            var next = list.stream().filter(doc->doc.state() == Document.DocumentState.DELETE).collect(Collectors.toSet());
            subject.next(next);
        });
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
