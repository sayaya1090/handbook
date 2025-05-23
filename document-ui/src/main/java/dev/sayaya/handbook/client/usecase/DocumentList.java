package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

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
        //var deleteCandidates = typeListToDelete.getValue();
        Arrays.stream(documents).filter(Objects::nonNull)
        //        .filter(type -> !deleteCandidates.contains(type))
                .forEach(container::add);
        this.subject.next(container.stream().collect(Collectors.toUnmodifiableList()));
    }
    public void remove(Document... documents) {
        for(var document: documents) container.remove(document);
        this.subject.next(container.stream().collect(Collectors.toUnmodifiableList()));
    }
    public <T> Observable<T> map(Function<List<Document>, T> mapper) {
        return subject.map(mapper);
    }
    public Subscription subscribe(Consumer<List<Document>> consumer) {
        return subject.subscribe(consumer);
    }
}
