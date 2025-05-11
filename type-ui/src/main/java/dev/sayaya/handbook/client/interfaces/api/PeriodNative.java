package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Period;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.Date;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class PeriodNative {
    @JsProperty public String workspace;
    @JsProperty(name = "effect_date_time") public Double effectDateTime;
    @JsProperty(name = "expire_date_time") public Double expireDateTime;
    @JsOverlay @JsIgnore public Period toDomain() {
        return new Period(
            effectDateTime != null ? new Date(effectDateTime.longValue()) : null,
            expireDateTime != null ? new Date(expireDateTime.longValue()) : null
        );
    }
}
