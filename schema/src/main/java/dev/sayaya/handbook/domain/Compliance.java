package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Compliance {
    @JsonProperty("compatible") @JsProperty private boolean compatible;
    @JsonProperty("violations") @JsProperty private String[] violations;

    @JsOverlay @JsIgnore
    public static Compliance ok() {
        Compliance c = new Compliance();
        c.compatible = true;
        c.violations = new String[0];
        return c;
    }
}
