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
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
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
    @Inject DataProvider(DocumentList documents, TypeProvider type, ActionManager actionManager, UpdateDocumentEventSource eventSource) {
        this.documents = documents;
        this.type = type;
        this.actionManager = actionManager;
        documents.asObservable().debounceTime(100).map(this::mapToList).subscribe(subject::next);
        eventSource.subscribe(this::synchronize);
    }
    /**
     * Document 변경 이벤트를 받아 UI 데이터를 동기화합니다.
     * 서버에서 받은 데이터로 기존 데이터를 덮어쓰고, UI 상태(로컬 변경사항)는 초기화됩니다.
     */
    private void synchronize(HandbookEvent<Document> evt) {
        Document updatedDoc = evt.param();
        documents.getValue().stream()
                .filter(d -> Objects.equals(d.serial(), updatedDoc.serial()))
                .findFirst()
                .ifPresent(prevDoc -> {
                    cache.remove(prevDoc.id()); // 이전 Document에 해당하는 캐시를 명확히 제거합니다.
                    documents.replace(prevDoc, updatedDoc); // DocumentList에서 Document를 교체합니다. 이로 인해 반응형 스트림이 트리거되어 UI가 업데이트됩니다.
                });
    }

    private List<Data> mapToList(List<Document> docList) {
        // 현재 Document 목록에 없는 Data 객체들을 캐시에서 정리합니다.
        var currentDocIds = docList.stream().map(Document::id).collect(Collectors.toSet());
        cache.keySet().retainAll(currentDocIds);
        return docList.stream().map(this::mapToData).collect(Collectors.toUnmodifiableList());
    }

    private Data mapToData(Document document) {
        // 캐시에 없으면 새로 생성하고, 있으면 기존 객체를 사용합니다.
        Data data = cache.computeIfAbsent(document.id(), id -> createData(document));
        data.put("Serial", document.serial())
            .put("Effect date time", DTF.format(document.effectDateTime()))
            .put("Expire date time", DTF.format(document.expireDateTime()))
            .put("$state", document.state().name());
        var documentValues = document.values() != null ? document.values() : Collections.<String, Object>emptyMap();
        // data 객체에 있지만 새 document 값에는 없는 키 삭제
        data.keys().stream()
            .filter(key -> !Set.of("Serial", "Effect date time", "Expire date time", "$state").contains(key))
            .filter(key -> !documentValues.containsKey(key))
            .forEach(data::delete);
        // document 값으로 data 객체 업데이트/추가
        if (document.values() != null) for (var entry : document.values().entrySet()) {
            data.put(entry.getKey(), entry.getValue() != null ? String.valueOf(entry.getValue()) : null);
        }
        if (document.validations() != null) document.validations().values().forEach(data::validity);
        else data.keys().forEach(key -> data.validity(key, null));
        return data;
    }

    private Data createData(Document document) {
        var data = Data.create(document.id());
        var typeDef = this.type.getValue();
        if (typeDef != null && typeDef.attributes() != null) typeDef.attributes().stream()
                .map(Attribute::name)
                .forEach(attrName -> {
                    Object value = document.values() != null ? document.values().get(attrName) : null;
                    data.put(attrName, value != null ? String.valueOf(value) : null);
                });
        Subject<String> subject = subject(String.class);
        subject.debounceTime(100).subscribe(s->notifyChange(data, document.id()));
        data.onValueChange(s->{
            if(!"$state".equals(s.value())) subject.next(s.value());
        });
        return data;
    }

    /**
     * UI에서 값이 변경되었을 때 호출되어 변경사항을 처리합니다.
     */
    private void notifyChange(Data data, String id) {
        documents.getValue().stream()
                .filter(d -> d.id().equals(id)).findFirst()
                .ifPresent(origin -> {
                    Document next = buildModifiedDocument(data, origin);
                    if (!isDocumentEqual(origin, next)) actionManager.edit(origin, next);
                });
    }

    /**
     * UI의 Data 객체와 원본 Document를 기반으로 수정된 Document 객체를 생성합니다.
     */
    private Document buildModifiedDocument(Data data, Document origin) {
        var effectDateTimeDbl = JsDate.parse(data.get("Effect date time"));
        var effectDateTime = !Double.isNaN(effectDateTimeDbl) ? new Date((long) effectDateTimeDbl) : origin.effectDateTime();
        var expireDateTimeDbl = JsDate.parse(data.get("Expire date time"));
        var expireDateTime = !Double.isNaN(expireDateTimeDbl) ? new Date((long) expireDateTimeDbl) : origin.expireDateTime();
        var builder = origin.toBuilder()
                .serial(data.get("Serial"))
                .effectDateTime(effectDateTime)
                .expireDateTime(expireDateTime)
                .state(data.isChanged() ? Document.DocumentState.CHANGE : Document.DocumentState.NOT_CHANGE);

        for (String key : data.keys()) {
            if (Set.of("$state", "Serial", "Effect date time", "Expire date time").contains(key)) continue;
            String value = data.get(key);
            builder.value(key, (value != null && !value.isEmpty()) ? value : null);
        }
        return builder.build();
    }

    /**
     * 두 Document 객체의 내용이 같은지 비교합니다.
     */
    private boolean isDocumentEqual(Document a, Document b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.id(), b.id()) &&
                Objects.equals(a.type(), b.type()) &&
                Objects.equals(a.serial(), b.serial()) &&
                Objects.equals(a.effectDateTime(), b.effectDateTime()) &&
                Objects.equals(a.expireDateTime(), b.expireDateTime()) &&
                Objects.equals(a.values(), b.values());
    }
}
