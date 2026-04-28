package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class TypeLayout {
    @JsonProperty("workspaceId") @JsProperty public String workspaceId;
    @JsonProperty("positions") @JsProperty public JsPropertyMap<Position> positions;
}
