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
public final class TypeLayout {
    @JsonProperty("workspaceId") @JsProperty private String workspaceId;
    @JsonProperty("positions") @JsProperty private JsPropertyMap<Position> positions;
}
