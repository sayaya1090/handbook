package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="Object")
@Getter(onMethod_ = { @JsOverlay, @JsIgnore })
@Accessors(fluent = true)
public final class Progress {
    private boolean enabled;
    private boolean intermediate;
    private Double value;
    private Double max;

    @JsOverlay @JsIgnore
    static Progress of(boolean enabled, boolean intermediate, Double value, Double max) {
        Progress progress = new Progress();
        progress.enabled = enabled;
        progress.intermediate = intermediate;
        progress.value = value;
        progress.max = max;
        return progress;
    }
    @JsOverlay @JsIgnore
    public static ProgressBuilder builder() { return new ProgressBuilder(); }
}
