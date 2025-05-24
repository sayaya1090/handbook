package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class DataProvider {
    @Delegate private final BehaviorSubject<List<Data>> subject = behavior(List.of());
    private final ActionManager actionManager;
    @Inject DataProvider(DocumentList documents, ActionManager actionManager) {
        this.actionManager = actionManager;
        documents.map(this::map).subscribe(subject::next);
    }
    private List<Data> map(List<Document> documents) {
        return documents.stream().map(this::map).collect(Collectors.toUnmodifiableList());
    }
    private Data map(Document document) {
        Data data = Data.create(document.id());
        data.put("Serial", document.serial());
        JsDate eff = JsDate.create(document.effectDateTime().getTime());
        JsDate exp = JsDate.create(document.expireDateTime().getTime());
        data.put("Effect date time", eff.toLocaleDateString());
        data.put("Expire date time", exp.toLocaleDateString());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            if(entry.getValue()!=null) data.put(entry.getKey(), String.valueOf(entry.getValue()));
            else data.put(entry.getKey(), null);
        }
        if(document.state()!=Document.DocumentState.DELETE) data.onValueChange(evt->{
            var effectDateTimeDbl = JsDate.parse(data.get("Effect date time"));
            var effectDateTime = Double.isNaN(effectDateTimeDbl) ? null : Double.valueOf(effectDateTimeDbl).longValue();
            var expireDateTimeDbl = JsDate.parse(data.get("Expire date time"));
            var expireDateTime = Double.isNaN(expireDateTimeDbl) ? null : Double.valueOf(expireDateTimeDbl).longValue();
            var next = Document.builder().id(document.id()).type(document.type())
                    .createdDateTime(document.createdDateTime())
                    .effectDateTime(effectDateTime!=null ? new Date(effectDateTime) : document.effectDateTime())
                    .expireDateTime(expireDateTime!=null ? new Date(expireDateTime) : document.expireDateTime())
                    .createdBy(document.createdBy())
                    .serial(data.get("Serial"))
                    .state(data.isChanged() ? Document.DocumentState.CHANGE : Document.DocumentState.NOT_CHANGE);
            for(var entry: document.values().entrySet()) {
                var value = data.get(entry.getKey());
                next.value(entry.getKey(), value!=null && !value.isEmpty() ? value : null);
            }
            actionManager.edit(document, next.build());
        });
        return data;
    }
}
