package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class LayoutPeriod {
    @JsonProperty("effectDateTime") @JsProperty private double effectDateTime;
    @JsonProperty("expireDateTime") @JsProperty private double expireDateTime;

    @JsOverlay @JsIgnore
    public static LayoutPeriod of(double start, double end) {
        LayoutPeriod period = new LayoutPeriod();
        period.effectDateTime(start);
        period.expireDateTime(end);
        return period;
    }

    @JsOverlay
    public final double overlap(LayoutPeriod other) {
        double start = Math.max(effectDateTime(), other.effectDateTime());
        double end = Math.min(expireDateTime(), other.expireDateTime());
        return Math.max(0, end - start);
    }
}
