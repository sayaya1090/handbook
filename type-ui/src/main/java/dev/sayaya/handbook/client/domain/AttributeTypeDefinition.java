package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

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
}
