package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.client.domain.validator.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
    public String simplify() {
        switch (baseType) {
            case Value -> {
                if(validators.isEmpty() || validators.get(0) instanceof ValidatorRegex) return "String";
                else if(validators.get(0) instanceof ValidatorBool) return "Boolean";
                else if(validators.get(0) instanceof ValidatorNumber) return "Number";
                else if(validators.get(0) instanceof ValidatorDate) return "Date";
                else if(validators.get(0) instanceof ValidatorSelect) return "Select";
                return "Value";
            }
            case Array -> {
                return "Array<" + arguments.get(0).simplify() + ">";
            }
            case Map -> {
                return "Map<" + arguments.get(0).simplify() + ", " + arguments.get(1).simplify() + ">";
            }
            case File -> {
                return "File(" + String.join(", ", extensions) + ")";
            }
            case Document -> {
                return referencedType;
            }
            default -> {
                return "Unknown";
            }
        }
    }
}
