package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition.AttributeType;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.util.Arrays;
import java.util.List;
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
                .arguments(arguments!=null? Arrays.stream(arguments).map(AttributeTypeDefinitionNative::toDomain).collect(Collectors.toList()) : List.of())
                .validators(validators!=null? Arrays.stream(validators).map(ValidatorDefinitionNative::toDomain).collect(Collectors.toList()) : List.of())
                .extensions(extensions != null ? Set.of(extensions) : Set.of())
                .referencedType(referencedType)
                .build();
    }
}
