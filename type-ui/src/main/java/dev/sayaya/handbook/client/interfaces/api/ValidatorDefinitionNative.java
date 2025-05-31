package dev.sayaya.handbook.client.interfaces.api;


import dev.sayaya.handbook.client.domain.validator.ValidatorDefinition;
import dev.sayaya.handbook.client.domain.validator.ValidatorRegex;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class ValidatorDefinitionNative {
    @JsProperty private String type;
    @JsProperty private String pattern;
    @JsOverlay @JsIgnore
    public ValidatorDefinition toDomain() {
        if( type == null) return null;
        else if ("REGEX".equals(type)) return ValidatorRegex.builder().pattern(pattern).build();
        return null;
    }
    @JsOverlay @JsIgnore
    public static ValidatorDefinitionNative from(ValidatorDefinition domainObj) {
        if (domainObj == null) return null;
        else if(domainObj instanceof ValidatorRegex) return from((ValidatorRegex) domainObj);
        else return null;
    }
    @JsOverlay @JsIgnore
    private static ValidatorDefinitionNative from(ValidatorRegex domainObj) {
        if (domainObj == null) return null;
        ValidatorDefinitionNative nativeDef = new ValidatorDefinitionNative();
        nativeDef.type = "REGEX";
        nativeDef.pattern = domainObj.pattern();
        return nativeDef;
    }
}
