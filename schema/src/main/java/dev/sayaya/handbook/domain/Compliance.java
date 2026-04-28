package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Compliance {
    @JsonProperty("compatible") @JsProperty public boolean compatible;
    @JsonProperty("violations") @JsProperty public String[] violations;

    @JsOverlay @JsIgnore
    public static Compliance ok() {
        Compliance c = new Compliance();
        c.compatible = true;
        c.violations = new String[0];
        return c;
    }
}
