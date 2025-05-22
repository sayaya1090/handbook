package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class DocumentList {
    @Delegate private final BehaviorSubject<List<Document>> subject = behavior(List.of());
    @Inject DocumentList() {}
}
