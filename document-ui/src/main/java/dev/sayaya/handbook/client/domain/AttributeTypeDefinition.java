package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.client.domain.validator.ValidatorDefinition;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@Getter
@ToString(exclude = {"referencedType"})
@EqualsAndHashCode(exclude = {"referencedType"})
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class AttributeTypeDefinition {
    private final AttributeType baseType;
    @Singular private final List<AttributeTypeDefinition> arguments;
    @Singular private final List<ValidatorDefinition> validators;
    @Singular private final Set<String> extensions;
    @Setter private String referencedType;
    public enum AttributeType {
        Value,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
        Array,  // x개 값
        Map,    // Key-Value 형태
        File,
        Document
    }
}
