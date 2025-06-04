package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.Date;
import java.util.HashMap;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class DocumentNative {
    @JsProperty public String id;
    @JsProperty public String serial;
    @JsProperty public String type;
    @JsProperty(name = "create_date_time") public Double createDateTime;
    @JsProperty(name = "effect_date_time") public Double effectDateTime;
    @JsProperty(name = "expire_date_time") public Double expireDateTime;
    @JsProperty public String creator;
    @JsProperty public JsObject data;
    @JsProperty public ValidationNative validations;

    @JsOverlay @JsIgnore public Document toDomain() {
        var builder = Document.builder()
                .id(id).serial(serial).type(type)
                .effectDateTime(effectDateTime != null ? new Date(effectDateTime.longValue()) : null)
                .expireDateTime(expireDateTime != null ? new Date(expireDateTime.longValue()) : null)
                .createdDateTime(createDateTime != null ? new Date(createDateTime.longValue()) : null)
                .createdBy(creator)
                .validations(validations!=null?validations.toDomain():null);
        if(data!=null) {
            JsPropertyMap<String> map = Js.cast(data);
            map.forEach(key -> {
                var value = map.get(key);
                if(value != null) builder.value(key, value);
            });
        } else builder.values(new HashMap<>());
        return builder.build();
    }
    @JsOverlay @JsIgnore public static DocumentNative from(Document document) {
        if (document == null) return null;
        var nativeDocument = new DocumentNative();
        nativeDocument.id = document.createdBy() == null? null : document.id();
        nativeDocument.serial = document.serial();
        nativeDocument.type = document.type();
        nativeDocument.effectDateTime = document.effectDateTime()!=null?Long.valueOf(document.effectDateTime().getTime()).doubleValue():null;
        nativeDocument.expireDateTime = document.expireDateTime()!=null?Long.valueOf(document.expireDateTime().getTime()).doubleValue():null;
        nativeDocument.createDateTime = document.createdDateTime()!=null?Long.valueOf(document.createdDateTime().getTime()).doubleValue():null;
        nativeDocument.creator = document.createdBy();
        if (document.values() != null) {
            JsPropertyMap<Object> map = JsPropertyMap.of();
            document.values().forEach((key, value) -> {
                if (value != null) map.set(key, value);
            });
            nativeDocument.data = Js.cast(map);
        }
        return nativeDocument;
    }
}
