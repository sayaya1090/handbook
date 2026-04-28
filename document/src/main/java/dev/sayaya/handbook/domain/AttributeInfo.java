package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class AttributeInfo {
    @JsonProperty("name") @JsProperty private String name;
    @JsonProperty("type") @JsProperty private String type;
    @JsonProperty("nullable") @JsProperty private boolean nullable;
    @JsonProperty("description") @JsProperty private String description;
}
