package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import jsinterop.base.JsPropertyMap;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class DocumentValue {
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("type") @JsProperty private String type;
    @JsonProperty("serial") @JsProperty private String serial;
    @JsonProperty("effectDateTime") @JsProperty private double effectDateTime;
    @JsonProperty("expireDateTime") @JsProperty private double expireDateTime;
    @JsonProperty("createDateTime") @JsProperty private double createDateTime;
    @JsonProperty("creator") @JsProperty private String creator;
    @JsonProperty("data") @JsProperty private JsPropertyMap<String> data;
    @JsonProperty("status") @JsProperty private String status;
    @JsonProperty("rev") @JsProperty private double rev;

    @JsOverlay
    public final boolean isExpired(double now) {
        return expireDateTime > 0 && expireDateTime <= now;
    }

    @JsOverlay @JsIgnore
    public static DocumentValue create(String id, String type) {
        DocumentValue doc = new DocumentValue();
        doc.id = id;
        doc.type = type;
        doc.status = "DRAFT";
        return doc;
    }
}
