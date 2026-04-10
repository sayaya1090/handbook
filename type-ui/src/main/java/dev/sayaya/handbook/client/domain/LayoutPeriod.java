package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 레이아웃 기간. effectDateTime ~ expireDateTime 범위를 나타낸다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class LayoutPeriod {
    public double effectDateTime;
    public double expireDateTime;

    @JsOverlay
    public static LayoutPeriod of(double effectDateTime, double expireDateTime) {
        LayoutPeriod p = new LayoutPeriod();
        p.effectDateTime = effectDateTime;
        p.expireDateTime = expireDateTime;
        return p;
    }

    @JsOverlay
    public final double overlap(LayoutPeriod other) {
        double start = Math.max(effectDateTime, other.effectDateTime);
        double end = Math.min(expireDateTime, other.expireDateTime);
        return Math.max(0, end - start);
    }
}
