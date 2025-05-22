package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class DataProvider {
    @Delegate private final BehaviorSubject<List<Data>> subject = behavior(List.of());
    @Inject DataProvider(DocumentList documents) {
        documents.map(DataProvider::map).subscribe(subject::next);
    }
    private static List<Data> map(List<Document> documents) {
        return documents.stream().map(DataProvider::map).collect(Collectors.toUnmodifiableList());
    }
    private static Data map(Document document) {
        Data data = Data.create(document.id());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            if(entry.getValue()!=null) data.put(entry.getKey(), String.valueOf(entry.getValue()));
            else data.put(entry.getKey(), null);
        }
        return data;
    }
}
