package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition.*;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class AttributeTypeDefinitionNative {
    @JsProperty(name = "base_type") public String baseType;
    @JsProperty public AttributeTypeDefinitionNative[] arguments;
    @JsProperty public ValidatorDefinitionNative[] validators;
    @JsProperty public String[] extensions;
    @JsProperty(name = "referenced_type") public String referencedType;

    @JsOverlay @JsIgnore
    public AttributeTypeDefinition toDomain() {
        return AttributeTypeDefinition.builder()
                .baseType(AttributeType.valueOf(baseType))
                .arguments(arguments!=null? Arrays.stream(arguments).filter(Objects::nonNull).map(AttributeTypeDefinitionNative::toDomain).filter(Objects::nonNull).collect(Collectors.toList()) : List.of())
                .validators(validators!=null? Arrays.stream(validators).filter(Objects::nonNull)
                        .map(ValidatorDefinitionNative::toDomain)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()) : List.of())
                .extensions(extensions != null ? Set.of(extensions) : Set.of())
                .referencedType(referencedType)
                .build();
    }
    @JsOverlay @JsIgnore
    public static AttributeTypeDefinitionNative from(AttributeTypeDefinition domainObj) {
        if (domainObj == null) return null;
        AttributeTypeDefinitionNative nativeDef = new AttributeTypeDefinitionNative();
        nativeDef.baseType = domainObj.baseType().name();
        nativeDef.arguments = domainObj.arguments().stream().map(e->AttributeTypeDefinitionNative.from(e)).toArray(i->new AttributeTypeDefinitionNative[i]);
        nativeDef.validators = domainObj.validators().stream().map(e->ValidatorDefinitionNative.from(e)).toArray(i->new ValidatorDefinitionNative[i]);
        nativeDef.extensions = domainObj.extensions().stream().toArray(i->new String[i]);
        nativeDef.referencedType = domainObj.referencedType();
        return nativeDef;
    }
}
