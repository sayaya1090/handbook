package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public final class Value {
    private String id;
    private String name;
    private String type;
    private Box refer;
}
