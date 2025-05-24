package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
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
        data.put("Serial", document.serial());
        JsDate eff = JsDate.create(document.effectDateTime().getTime());
        JsDate exp = JsDate.create(document.expireDateTime().getTime());
        data.put("Effect date time", eff.toLocaleDateString());
        data.put("Expire date time", exp.toLocaleDateString());
        if(document.values()!=null) for(var entry: document.values().entrySet()) {
            if(entry.getValue()!=null) data.put(entry.getKey(), String.valueOf(entry.getValue()));
            else data.put(entry.getKey(), null);
        }
        data.onValueChange(evt->{
            var serial = data.get("Serial");
            document.serial(serial);
            for(var entry: document.values().entrySet()) {
                var value = data.get(entry.getKey());
                document.values().put(entry.getKey(), value);
            }
            if(document.state()!= Document.DocumentState.DELETE) {
                if(data.isChanged()) document.state(Document.DocumentState.CHANGE);
                else document.state(Document.DocumentState.NOT_CHANGE);
            }
            DomGlobal.console.log(data);
        });
        return data;
    }

    private static long fromUtcToLocalDatetime(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long) (date.getTime() - offset*60*1000);
    }
    /*
     else {
            JsDate cast = JsDate.create(date.getTime());
            ipt.element().valueAsNumber = (fromUtcToLocalDatetime(cast) / 1000) * 1000.0;
        }
    }
    private static long fromUtcToLocalDatetime(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long) (date.getTime() - offset*60*1000);
    }
     */
}
