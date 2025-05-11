package dev.sayaya.handbook.client.domain;

import java.io.Serializable;
import java.util.*;

public sealed interface AttributeTypeDefinition {
    AttributeType baseType();
    //Map<String, Serializable> constraints();
    enum AttributeType {
        VALUE,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
        ARRAY,  // x개 값
        MAP,    // Key-Value 형태
        FILE,
        DOCUMENT
    }

    record ValueType() implements AttributeTypeDefinition {
        @Override
        public AttributeType baseType() {
            return AttributeType.VALUE;
        }
    }
    record ArrayType(AttributeTypeDefinition type) implements AttributeTypeDefinition {
        @Override
        public AttributeType baseType() {
            return AttributeType.ARRAY;
        }
        public List<AttributeTypeDefinition> arguments() {
            return List.of(type);
        }
    }
    record MapType(AttributeTypeDefinition key, AttributeTypeDefinition value) implements AttributeTypeDefinition {
        @Override
        public AttributeType baseType() {
            return AttributeType.MAP;
        }
        public List<AttributeTypeDefinition> arguments() {
            return List.of(key, value);
        }
    }
    record FileType(Set<String> extensions) implements AttributeTypeDefinition {
        public FileType { // Compact constructor for validation
            Objects.requireNonNull(extensions, "extensions cannot be null");
            if (extensions.stream().anyMatch(ext -> ext == null || !ext.matches("^[a-zA-Z0-9]+$"))) {
                throw new IllegalArgumentException("FileAttribute extensions must contain only non-null alphanumeric characters.");
            }
        }
        @Override
        public AttributeType baseType() {
            return AttributeType.FILE;
        }
        /*@Override
        public Map<String, Serializable> constraints() {
            return Collections.emptyMap(); // FileType always has empty constraints
        }*/
    }
    record DocumentType(String referencedType, Map<String, Serializable> constraints) implements AttributeTypeDefinition {
        public DocumentType(String referencedType) {
            this(referencedType, Collections.emptyMap());
        }
        public DocumentType { // Compact constructor for validation
            Objects.requireNonNull(referencedType, "referencedType cannot be null");
        }
        @Override
        public AttributeType baseType() {
            return AttributeType.DOCUMENT;
        }
    }
}
