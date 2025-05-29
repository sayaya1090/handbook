package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@Accessors(fluent = true)
@Builder
public class Attribute {
    private String id;
    private String name;
    private AttributeTypeDefinition type;
    private Type parent;
    private String description;
    private boolean nullable;
    private boolean inherited;
    @Builder.Default
    private AttributeState state = AttributeState.NOT_CHANGE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute cast = (Attribute) o;
        return Objects.equals(id, cast.id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    public enum AttributeState {
        NOT_CHANGE, CHANGE, DELETE
    }
}
