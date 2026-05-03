package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import jsinterop.base.JsPropertyMap;
import lombok.*;
import lombok.experimental.Accessors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Document {
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("type") @JsProperty private String type;
    @JsonProperty("serial") @JsProperty private String serial;
    @JsonProperty("effectDateTime") @JsProperty private double effectDateTime;
    @JsonProperty("expireDateTime") @JsProperty private double expireDateTime;
    @JsonProperty("createDateTime") @JsProperty private double createDateTime;
    @JsonProperty("creator") @JsProperty private String creator;
    @JsonProperty("data") @JsProperty private JsPropertyMap<String> data;
    @JsonProperty("status") @JsProperty private String status;
    @JsonProperty("rev") @JsProperty private long rev;

    @JsOverlay @JsIgnore
    public static Document create(String id, String type) {
        return create(id, type, null, 0.0, 0.0, 0.0, null, null);
    }

    @JsOverlay @JsIgnore
    public static Document create(String id, String type, String serial) {
        return create(id, type, serial, 0.0, 0.0, 0.0, null, null);
    }

    @JsOverlay @JsIgnore
    public boolean isExpired(double now) {
        return expireDateTime() > 0 && expireDateTime() <= now;
    }

    @JsOverlay @JsIgnore
    public static Document create(String id, String type, String serial, double effectDateTime, double expireDateTime, double createDateTime, String creator, jsinterop.base.JsPropertyMap<String> data) {
        if (serial != null && !serial.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Document serial must be alphanumeric and may include hyphens and underscores.");
        }
        if (effectDateTime > 0 && expireDateTime > 0 && expireDateTime <= effectDateTime) {
            throw new IllegalArgumentException("Expire date time must be after effect date time");
        }
        if (id != null && (createDateTime <= 0 || creator == null)) {
            throw new IllegalArgumentException("If id is not null, createDateTime and creator must be not null");
        }
        Document doc = new Document();
        doc.id(id);
        doc.type(type);
        doc.serial(serial);
        doc.effectDateTime(effectDateTime);
        doc.expireDateTime(expireDateTime);
        doc.createDateTime(createDateTime);
        doc.creator(creator);
        doc.data(data);
        doc.status("DRAFT");
        doc.rev(-1);
        return doc;
    }

    @JsOverlay @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        if (id() == null || document.id() == null) return false;
        return id().equals(document.id());
    }

    @JsOverlay @Override
    public int hashCode() {
        if (id() != null) return id().hashCode();
        return System.identityHashCode(this);
    }
}
