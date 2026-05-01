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
public final class DocumentValue implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id") @JsProperty private String id;
    @JsonProperty("type") @JsProperty private String type;
    @JsonProperty("serial") @JsProperty private String serial;
    @JsonProperty("effectDateTime") @JsProperty private double effectDateTime;
    @JsonProperty("expireDateTime") @JsProperty private double expireDateTime;
    @JsonProperty("data") @JsProperty private JsPropertyMap<String> data;
    @JsonProperty("status") @JsProperty private String status;

    @JsOverlay @JsIgnore
    public static DocumentValue create(String id, String type) {
        DocumentValue doc = new DocumentValue();
        doc.id(id);
        doc.type(type);
        doc.status("DRAFT");
        return doc;
    }
}
