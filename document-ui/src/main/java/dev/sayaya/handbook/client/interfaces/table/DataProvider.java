package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.i18n.client.DateTimeFormat;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.HandbookEvent;
import dev.sayaya.handbook.client.interfaces.api.UpdateDocumentEventSource;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.rx.subject.Subject.subject;

@Singleton
public class DataProvider {
    private final Map<String, Data> cache = new ConcurrentHashMap<>();
    @Delegate private final BehaviorSubject<List<Data>> subject = behavior(List.of());
    private final DocumentList documents;
    private final TypeProvider type;
    private final ActionManager actionManager;
    private final DateTimeFormat DTF = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
    private final Map<String, Document> currentDocuments = new ConcurrentHashMap<>();
    @Inject DataProvider(DocumentList documents, TypeProvider type, ActionManager actionManager, UpdateDocumentEventSource eventSource) {
        this.documents = documents;
        this.type = type;
        this.actionManager = actionManager;
        documents.asObservable().debounceTime(100).map(this::map).subscribe(subject::next);
        eventSource.subscribe(this::synchronize);
    }
    private void synchronize(HandbookEvent<Document> evt) {
        var doc = evt.param();
        var prev = currentDocuments.values().stream().filter(d->d.serial().equals(doc.serial())).findFirst().orElse(null);
        if(prev == null) return;
        var data = cache.get(prev.id());    // Document event notify때문에 무조건 data를 새로 생성하는 로직으로 고쳐야 함
        var builder = prev.toBuilder();
        for(var key: data.keys()) {
            if("$state".equals(key)) continue;
            var value = doc.values().get(key);
            data.initialize(key, value!=null? String.valueOf(value) : null);
            boolean equals = Objects.equals(prev.values().get(key), value);
            if(equals) continue;
            if(data.isChanged(key)) data.put(key, String.valueOf(value));
            builder.value(key, value);
        }
        data.initialize("Serial", doc.serial())
            .initialize("Effect date time", DTF.format(doc.effectDateTime()))
            .initialize("Expire date time", DTF.format(doc.expireDateTime()));
        builder.id(doc.id())
               .serial(doc.serial())
               .effectDateTime(doc.effectDateTime())
               .expireDateTime(doc.expireDateTime())
               .createdDateTime(doc.createdDateTime())
               .createdBy(doc.createdBy())
               .validations(doc.validations())
               .state(data.isChanged() ? Document.DocumentState.CHANGE : Document.DocumentState.NOT_CHANGE);
        var next = builder.build();
        cache.remove(prev.id());
        cache.put(doc.id(), data);
        currentDocuments.remove(prev.id());
        documents.replace(prev, next);  // 취소할 수 없다
    }

    private List<Data> map(List<Document> documents) {
        return documents.stream().map(this::map).collect(Collectors.toUnmodifiableList());
    }
    private Data map(Document document) {
        currentDocuments.put(document.id(), document);
        Data data = cache.computeIfAbsent(document.id(), key->create(document.id()));
        data.put("Serial", document.serial())
            .put("Effect date time", DTF.format(document.effectDateTime()))
            .put("Expire date time", DTF.format(document.expireDateTime()))
            .put("$state", document.state().name());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            String key = entry.getKey();
            if("$state".equals(key)) continue;
            if(entry.getValue()!=null) data.put(key, String.valueOf(entry.getValue()));
            else data.put(key, null);
        }
        if(document.validations()!=null) for(var entry: document.validations().values().entrySet()) {
            String key = entry.getKey();
            if("$state".equals(key)) continue;
            if(entry.getValue()!=null) data.validity(key, entry.getValue());
            else data.validity(key, null);
        } else for(var key: data.keys()) data.validity(key, null);
        return data;
    }
    private Data create(String id) {
        var data = Data.create(id);
        var type = this.type.getValue();
        type.attributes().stream().map(Attribute::name).forEach(attr ->{
            Object value = currentDocuments.get(id).values().get(attr);
            if(value!=null) data.put(attr, String.valueOf(value));
            else data.put(attr, null);
        });
        Subject<String> subject = subject(String.class);
        subject.debounceTime(100).subscribe(s->notifyChange(data, id));
        data.onValueChange(s->{
            if(!"$state".equals(s.value())) subject.next(s.value());
        });
        return data;
    }
    private void notifyChange(Data data, String id) {
        var origin = currentDocuments.get(id);
        var effectDateTimeDbl = JsDate.parse(data.get("Effect date time"));
        var effectDateTime = Double.isNaN(effectDateTimeDbl) ? null : Double.valueOf(effectDateTimeDbl).longValue();
        var expireDateTimeDbl = JsDate.parse(data.get("Expire date time"));
        var expireDateTime = Double.isNaN(expireDateTimeDbl) ? null : Double.valueOf(expireDateTimeDbl).longValue();
        var builder = Document.builder().id(origin.id()).type(origin.type())
                .createdDateTime(origin.createdDateTime())
                .effectDateTime(effectDateTime!=null ? new Date(effectDateTime) : origin.effectDateTime())
                .expireDateTime(expireDateTime!=null ? new Date(expireDateTime) : origin.expireDateTime())
                .createdBy(origin.createdBy())
                .serial(data.get("Serial"))
                .state(data.isChanged() ? Document.DocumentState.CHANGE : Document.DocumentState.NOT_CHANGE);
        for(var key: data.keys()) {
            if("$state".equals(key) ||
               "Serial".equals(key) ||
               "Effect date time".equals(key) ||
               "Expire date time".equals(key)) continue;
            var value = data.get(key);
            if(value!=null && value.isEmpty()) value = null;
            builder.value(key, value);
        }
        var next = builder.build();
        if(!equals(origin, next)) actionManager.edit(origin, next);
    }
    private boolean equals(Document a, Document b) {
        return Objects.equals(a.id(), b.id()) &&
                Objects.equals(a.type(), b.type()) &&
                Objects.equals(a.effectDateTime().getTime(), b.effectDateTime().getTime()) &&
                Objects.equals(a.expireDateTime().getTime(), b.expireDateTime().getTime()) &&
                Objects.equals(a.serial(), b.serial()) &&
                Objects.equals(a.values(), b.values());
    }
}
