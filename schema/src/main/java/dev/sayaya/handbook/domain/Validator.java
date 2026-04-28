package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jsinterop.annotations.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Validator.TextValidator.class, name = "text"),
    @JsonSubTypes.Type(value = Validator.NumberValidator.class, name = "number")
})
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public abstract class Validator {
    @JsonProperty("type") @JsProperty public String type;

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static final class TextValidator extends Validator {
        @JsonProperty("minLength") @JsProperty public Integer minLength;
        @JsonProperty("maxLength") @JsProperty public Integer maxLength;
        @JsonProperty("regex") @JsProperty public String regex;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static final class NumberValidator extends Validator {
        @JsonProperty("min") @JsProperty public Double min;
        @JsonProperty("max") @JsProperty public Double max;
    }
}
