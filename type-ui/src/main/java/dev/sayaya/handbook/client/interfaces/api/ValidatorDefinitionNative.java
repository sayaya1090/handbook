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
        // else if ("DATE".equals(type)) return ValidatorDefinition.Date();
        else if ("ENUM".equals(type)) return ValidatorSelect.builder().options(options).build();
        return null;
    }
    @JsOverlay @JsIgnore
    public static ValidatorDefinitionNative from(ValidatorDefinition domainObj) {
        if (domainObj == null) return null;
        else if(domainObj instanceof ValidatorRegex cast) return from(cast);
        else if(domainObj instanceof ValidatorBool cast) return from(cast);
        else if(domainObj instanceof ValidatorNumber cast) return from(cast);
        else if(domainObj instanceof ValidatorSelect cast) return from(cast);
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
    @JsOverlay @JsIgnore
    private static ValidatorDefinitionNative from(ValidatorBool domainObj) {
        if (domainObj == null) return null;
        ValidatorDefinitionNative nativeDef = new ValidatorDefinitionNative();
        nativeDef.type = "BOOL";
        return nativeDef;
    }
    @JsOverlay @JsIgnore
    private static ValidatorDefinitionNative from(ValidatorNumber domainObj) {
        if (domainObj == null) return null;
        ValidatorDefinitionNative nativeDef = new ValidatorDefinitionNative();
        nativeDef.type = "NUMBER";
        nativeDef.min = domainObj.min();
        nativeDef.max = domainObj.max();
        return nativeDef;
    }
    @JsOverlay @JsIgnore
    private static ValidatorDefinitionNative from(ValidatorSelect domainObj) {
        if (domainObj == null) return null;
        ValidatorDefinitionNative nativeDef = new ValidatorDefinitionNative();
        nativeDef.type = "ENUM";
        nativeDef.options = domainObj.options();
        return nativeDef;
    }
}
