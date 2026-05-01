package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Validator.TextValidator.class, name = "text"),
    @JsonSubTypes.Type(value = Validator.NumberValidator.class, name = "number")
})
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public abstract class Validator {
    @JsonProperty("type") @JsProperty private String type;

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    @Getter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Accessors(fluent = true)
    public static final class TextValidator extends Validator {
        @JsonProperty("minLength") @JsProperty private Integer minLength;
        @JsonProperty("maxLength") @JsProperty private Integer maxLength;
        @JsonProperty("regex") @JsProperty private String regex;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    @Getter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Accessors(fluent = true)
    public static final class NumberValidator extends Validator {
        @JsonProperty("min") @JsProperty private Double min;
        @JsonProperty("max") @JsProperty private Double max;
    }
}
