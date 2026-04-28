package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class LayoutPeriod {
    @JsonProperty("effectDateTime") @JsProperty public double effectDateTime;
    @JsonProperty("expireDateTime") @JsProperty public double expireDateTime;

    @JsOverlay @JsIgnore
    public static LayoutPeriod of(double start, double end) {
        LayoutPeriod period = new LayoutPeriod();
        period.effectDateTime = start;
        period.expireDateTime = end;
        return period;
    }

    /** 도메인 로직: 기간 중첩 계산 */
    @JsOverlay
    public final double overlap(LayoutPeriod other) {
        double start = Math.max(effectDateTime, other.effectDateTime);
        double end = Math.min(expireDateTime, other.expireDateTime);
        return Math.max(0, end - start);
    }
}
