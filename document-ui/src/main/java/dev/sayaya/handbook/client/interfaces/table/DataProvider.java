package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.event.shared.HandlerRegistration;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Document;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.rx.subject.Subject.subject;

@Singleton
public class DataProvider {
    private final Map<String, Data> cache = new HashMap<>();
    @Delegate private final BehaviorSubject<List<Data>> subject = behavior(List.of());
    private final TypeProvider type;
    private final ActionManager actionManager;
    @Inject DataProvider(DocumentList documents, TypeProvider type, ActionManager actionManager) {
        this.type = type;
        this.actionManager = actionManager;
        documents.asObservable().debounceTime(100).map(this::map).subscribe(subject::next);
    }
    private List<Data> map(List<Document> documents) {
        return documents.stream().map(this::map).collect(Collectors.toUnmodifiableList());
    }
    private Data map(Document document) {
        Data data = cache.computeIfAbsent(document.id(), key->create(document));
        data.put("Serial", document.serial());
        JsDate eff = JsDate.create(document.effectDateTime().getTime());
        JsDate exp = JsDate.create(document.expireDateTime().getTime());
        data.put("Effect date time", eff.toLocaleDateString());
        data.put("Expire date time", exp.toLocaleDateString());
        data.put("$state", document.state().name());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            if(entry.getValue()!=null) data.put(entry.getKey(), String.valueOf(entry.getValue()));
            else data.put(entry.getKey(), null);
        }
        return data;
    }
    private Data create(Document document) {
        var instance = Data.create(document.id());
        var type = this.type.getValue();
        type.attributes().stream().map(Attribute::name).forEach(attr ->{
            Object value = document.values().get(attr);
            if(value!=null) instance.put(attr, String.valueOf(value));
            else instance.put(attr, null);
        });
        Subject<String> subject = subject(String.class);
        subject.debounceTime(100).subscribe(s->notifyChange(instance, document));
        instance.onValueChange(s->subject.next(s.value()));
        return instance;
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
            var value = data.get(key);
            builder.value(key, value);
        }
        var next = builder.build();
        if(!origin.equals(next)) {
            DomGlobal.console.log("Document changed: "+origin+" -> "+next);
            actionManager.edit(origin, next);
        }
    }
}
