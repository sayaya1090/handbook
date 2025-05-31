package dev.sayaya.handbook.client.domain;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Attribute {
    private final String id;
    private final String name;
    private final AttributeTypeDefinition type;
    private final Type parent;
    private final String description;
    private final boolean nullable;
    private final boolean inherited;
}
