package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.rx.subject.Subject;
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
    private final TypeProvider type;
    private final ActionManager actionManager;
    @Inject DataProvider(DocumentList documents, TypeProvider type, ActionManager actionManager) {
        this.type = type;
        this.actionManager = actionManager;
        documents.asObservable().debounceTime(300).map(this::map).subscribe(subject::next);
    }
    private List<Data> map(List<Document> documents) {
        return documents.stream().map(this::map).collect(Collectors.toUnmodifiableList());
    }
    private Data map(Document document) {
        Data data = cache.computeIfAbsent(document.id(), key->create(document));
        JsDate eff = JsDate.create(document.effectDateTime().getTime());
        JsDate exp = JsDate.create(document.expireDateTime().getTime());
        data.put("Serial", document.serial())
            .put("Effect date time", eff.toLocaleString())
            .put("Expire date time", exp.toLocaleString())
            .put("$state", document.state().name());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            String key = entry.getKey();
            if("$state".equals(key) || "initializedValues".equals(key) || "stateChangeListeners".equals(key) || "valueChangeListeners".equals(key)) continue;
            if(entry.getValue()!=null) data.put(key, String.valueOf(entry.getValue()));
            else data.put(key, null);
        }
        return data;
    }
    private Data create(Document document) {
        var data = Data.create(document.id());
        var type = this.type.getValue();
        type.attributes().stream().map(Attribute::name).forEach(attr ->{
            Object value = document.values().get(attr);
            if(value!=null) data.put(attr, String.valueOf(value));
            else data.put(attr, null);
        });
        Subject<String> subject = subject(String.class);
        subject.debounceTime(100).subscribe(s->notifyChange(data, document));
        data.onValueChange(s->{
            if(!"$state".equals(s.value())) subject.next(s.value());
        });
        return data;
    }
    private void notifyChange(Data data, Document origin) {
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
               "initializedValues".equals(key) ||
               "stateChangeListeners".equals(key) ||
               "valueChangeListeners".equals(key) ||
               "Serial".equals(key) ||
               "Effect date time".equals(key) ||
               "Expire date time".equals(key)) continue;
            var value = data.get(key);
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
