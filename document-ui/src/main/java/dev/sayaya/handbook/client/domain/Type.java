package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Type {
    private final String id;
    private final String version;
    private final String prev;
    private final String next;
    private final Date effectDateTime;
    private final Date expireDateTime;
    private final String description;
    private final boolean primitive;
    @Singular("attribute")
    private final List<Attribute> attributes;
    private final String parent;

    private Type(String id, String version, String prev, String next,
                 Date effectDateTime, Date expireDateTime, String description,
                 boolean primitive, List<Attribute> attributes, String parent) {
        this.id = validateNonNullOrEmpty(id, "id");
        this.version = validateNonNullOrEmpty(version, "version");
        this.prev = prev;
        this.next = next;
        this.effectDateTime = validateNonNull(effectDateTime, "Effect date time");
        this.expireDateTime = validateNonNull(expireDateTime, "Expire date time");
        this.description = description;
        this.primitive = primitive;
        this.attributes = validateNonNull(attributes, "attributes");
        this.parent = parent;
        require(expireDateTime.after(effectDateTime), "Expire date time must be after effect date time");
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return Objects.equals(id, type.id) && Objects.equals(version, type.version);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    private static <T> T validateNonNull(T obj, String message) {
        require(obj != null, message + " must not be null");
        return obj;
    }
    private static String validateNonNullOrEmpty(String obj, String message) {
        require(obj != null && !obj.isEmpty(), message + " must not be null");
        return obj;
    }
}