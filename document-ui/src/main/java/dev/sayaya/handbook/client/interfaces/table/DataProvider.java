package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.i18n.client.DateTimeFormat;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.HandbookEvent;
import dev.sayaya.handbook.client.domain.validator.*;
import dev.sayaya.handbook.client.interfaces.api.DeleteDocumentEventSource;
import dev.sayaya.handbook.client.interfaces.api.UpdateDocumentEventSource;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnBuilder;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
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
    private final DocumentSelectedList selections;
    private final TypeProvider type;
    private final ActionManager actionManager;
    private final DateTimeFormat DTF = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
    @Inject DataProvider(DocumentList documents, DocumentSelectedList selections, TypeProvider type, ActionManager actionManager, UpdateDocumentEventSource eventSource, DeleteDocumentEventSource deleteEventSource) {
        this.documents = documents;
        this.selections = selections;
        this.type = type;
        this.actionManager = actionManager;
        documents.asObservable().debounceTime(100).map(this::mapToList).subscribe(subject::next);
        eventSource.subscribe(this::synchronize);
        deleteEventSource.subscribe(evt->{
            if(evt.param() == null) return;
            documents.getValue().stream()
                    .filter(d -> Objects.equals(d.serial(), evt.param().serial()))
                    .findFirst()
                    .ifPresent(documents::remove);
        });
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
        return docList.stream()
                .filter(doc->doc.isDelete() != Document.DocumentDeleteState.DELETE || doc.createdBy()!=null)    // 새로 만들었는데 삭제요청은 바로 화면에서 지우자. 어차피 업데이트/삭제 요청이 날아가지 않는다.
                .map(this::mapToData)
                .collect(Collectors.toUnmodifiableList());
    }

    private Data mapToData(Document document) {
        // 캐시에 없으면 새로 생성하고, 있으면 기존 객체를 사용합니다.
        Data data = cache.computeIfAbsent(document.id(), id -> createData(document));
        data.put("Serial", document.serial())
            .put("Effect date time", DTF.format(document.effectDateTime()))
            .put("Expire date time", DTF.format(document.expireDateTime()))
            .put("$state", document.isDelete().name());
        var documentValues = document.values() != null ? document.values() : Collections.<String, Object>emptyMap();
        // data 객체에 있지만 새 document 값에는 없는 키 삭제
        data.keys().stream()
            .filter(key -> !Set.of("Serial", "Effect date time", "Expire date time", "$state").contains(key))
            .filter(key -> !documentValues.containsKey(key))
            .forEach(data::delete);
        // document 값으로 data 객체 업데이트/추가
        var attrs = this.type.getValue().attributes().stream().collect(Collectors.toMap(Attribute::name, Attribute::type));
        if (document.values() != null) for (var entry : document.values().entrySet()) {
            Object value = entry.getValue();
            var attr = attrs.get(entry.getKey());
            String valueStr = toDataValue(value, attr);
            data.put(entry.getKey(), valueStr);
        }
        if (document.validations() != null) document.validations().values().forEach(data::validity);
        else data.keys().forEach(key -> data.validity(key, null));
        return data;
    }

    private Data createData(Document document) {
        var data = Data.create(document.id());
        var typeDef = this.type.getValue();
        if (typeDef != null && typeDef.attributes() != null) typeDef.attributes().stream()
                .forEach(attr -> {
                    Object value = document.values() != null ? document.values().get(attr.name()) : null;
                    String valueStr = toDataValue(value, attr.type());
                    data.put(attr.name(), valueStr);
                });
        Subject<String> subject = subject(String.class);
        subject.debounceTime(100).subscribe(s->notifyChange(data, document.id()));
        data.onValueChange(s->{
            if(!"$state".equals(s.value())) subject.next(s.value());
        });
        data.onStateChange(s->{
            if(data.state() == Data.DataState.SELECTED) selections.add(document);
            else selections.remove(document);
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
                .isChange(data.isChanged() ? Document.DocumentChangeState.CHANGE : Document.DocumentChangeState.NOT_CHANGE);
        var type = this.type.getValue();
        var attrs = type.attributes().stream().collect(Collectors.toMap(Attribute::name, Attribute::type));
        for (String key : data.keys()) {
            var attr = attrs.get(key);
            if (Set.of("$state", "Serial", "Effect date time", "Expire date time").contains(key)) continue;
            String value = data.get(key);
            builder.value(key, toDocumentValue(value, attr));
        }
        return builder.build();
    }
    private Object toDocumentValue(String value, AttributeTypeDefinition attr) {
        if(value==null || value.isEmpty()) return null;
        if(attr.baseType() == AttributeTypeDefinition.AttributeType.Value) {
            var validators = attr.validators();
            if(validators.isEmpty()) return value;
            else {
                var _validator = validators.get(0);
                if(_validator instanceof ValidatorRegex) return value;
                else if(_validator instanceof ValidatorBool) return Boolean.parseBoolean(value);
                else if(_validator instanceof ValidatorNumber) return Double.parseDouble(value);
                else if(_validator instanceof ValidatorDate) {
                    return value;
                } else if(_validator instanceof ValidatorEnum) {
                    return value;
                } else throw new RuntimeException("Unsupported attribute type: "+attr.baseType());
            }
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Array) {
            return splitAndUnescape(value);
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Map) {
            return value;
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.File) {
            return value;
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Document) {
            return value;
        } else throw new RuntimeException("Unsupported attribute type: "+attr.baseType());
    }
    private String toDataValue(Object value, AttributeTypeDefinition attr) {
        if(value==null) return null;
        if(attr.baseType() == AttributeTypeDefinition.AttributeType.Value) {
            var validators = attr.validators();
            if(validators.isEmpty()) return value.toString();
            else {
                var _validator = validators.get(0);
                if(_validator instanceof ValidatorRegex) return value.toString();
                else if(_validator instanceof ValidatorBool) return Boolean.toString((Boolean) value);
                else if(_validator instanceof ValidatorNumber) return Double.toString((Double) value);
                else if(_validator instanceof ValidatorDate) {
                    return value.toString();
                } else if(_validator instanceof ValidatorEnum) {
                    return value.toString();
                } else throw new RuntimeException("Unsupported attribute type: "+attr.baseType());
            }
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Array) {
            return Arrays.stream((String[])value).map(s->s.replace(",", "\\,")).collect(Collectors.joining(","));
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Map) {
            return value.toString();
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.File) {
            return value.toString();
        } else if(attr.baseType() == AttributeTypeDefinition.AttributeType.Document) {
            return value.toString();
        } else throw new RuntimeException("Unsupported attribute type: "+attr.baseType());
    }
    private String[] splitAndUnescape(String value) {
        if (value == null) return new String[0];
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length() && value.charAt(i + 1) == ',') {
                currentPart.append(',');
                i++; // 이스케이프된 쉼표를 처리했으므로 인덱스를 하나 더 증가시킵니다.
            } else if (c == ',') {
                parts.add(currentPart.toString().trim());
                currentPart.setLength(0);
            } else {
                currentPart.append(c);
            }
        }
        parts.add(currentPart.toString().trim());
        return parts.toArray(new String[0]);
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
