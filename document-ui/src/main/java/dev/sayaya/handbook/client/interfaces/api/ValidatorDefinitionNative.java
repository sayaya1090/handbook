package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.validator.*;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class ValidatorDefinitionNative {
    @JsProperty private String type;
    @JsProperty private String pattern;
    @JsProperty private Double min;
    @JsProperty private Double max;
    @JsProperty private String[] options;
    @JsOverlay @JsIgnore
    public ValidatorDefinition toDomain() {
        if( type == null) return null;
        else if ("REGEX".equals(type)) return ValidatorRegex.builder().pattern(pattern).build();
        else if ("BOOL".equals(type)) return ValidatorBool.builder().build();
        else if ("NUMBER".equals(type)) return ValidatorNumber.builder().min(min).max(max).build();
        else if ("DATE".equals(type)) return ValidatorDate.builder().build();
        else if ("ENUM".equals(type)) return ValidatorEnum.builder().options(options).build();
        return null;
    }
}
