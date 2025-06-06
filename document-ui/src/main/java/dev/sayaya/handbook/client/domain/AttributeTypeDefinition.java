package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Accessors(fluent = true)
@Builder
public class AttributeTypeDefinition {
    private AttributeType baseType;
    @Singular private List<AttributeTypeDefinition> arguments;
    @Singular private Set<String> extensions;
    private String referencedType;
    private Map<String, Serializable> constraints;
    public enum AttributeType {
        Value,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
        Array,  // x개 값
        Map,    // Key-Value 형태
        File,
        Document
    }
    @Override
    public String toString() {
        switch (baseType) {
            case Value -> {
                return "Value";
            }
            case Array -> {
                return "Array<" + arguments.get(0) + ">";
            }
            case Map -> {
                return "Map<" + arguments.get(0) + ", " + arguments.get(1) + ">";
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
