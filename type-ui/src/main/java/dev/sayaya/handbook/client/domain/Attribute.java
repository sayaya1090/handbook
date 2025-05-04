package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class Attribute {
    private String id;
    private String name;
    private String type;
    private Type parent;
}
