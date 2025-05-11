package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Type {
    private final String id;
    private final String version;
    private Date effectDateTime;
    private Date expireDateTime;
    private String description;
    private boolean primitive;
    @Singular("attribute")
    private List<Attribute> attributes;
    private String parent;
    private int x;
    private int y;
    private int width;
    private int height;

    private Type(String id, String version, Date effectDateTime, Date expireDateTime, String description, boolean primitive, List<Attribute> attributes, String parent, int x, int y, int width, int height) {
        this.id = validateNonNullOrEmpty(id, "id");
        this.version = validateNonNullOrEmpty(version, "version");
        effectDateTime(effectDateTime).expireDateTime(expireDateTime)
                .description(description).primitive(primitive)
                .attributes(attributes).parent(parent)
                .x(x).y(y).width(width).height(height);
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
    public Type effectDateTime(Date effectDateTime) {
        this.effectDateTime = validateNonNull(effectDateTime, "Effect date time must not be null");
        return this;
    }
    public Type expireDateTime(Date expireDateTime) {
        this.expireDateTime = validateNonNull(expireDateTime, "Expire date time must not be null");
        return this;
    }
    public Type attributes(List<Attribute> attributes) {
        this.attributes = validateNonNull(attributes, "attributes");
        return this;
    }
    public Type x(int x) {
        this.x = validateGreaterThanOrEqualZero(x, "X must be greater than or equal 0");
        return this;
    }
    public Type y(int y) {
        this.y = validateGreaterThanOrEqualZero(y, "Y must be greater than or equal 0");
        return this;
    }
    public Type width(int width) {
        this.width = validateGreaterThanZero(width, "Width must be greater than 0");
        return this;
    }

    public Type height(int height) {
        this.height = validateGreaterThanZero(height, "Height must be greater than 0");
        return this;
    }
    public void copyFrom(Type other) {
        require(other != null, "Other type must not be null");
        effectDateTime(other.effectDateTime).expireDateTime(other.expireDateTime)
                .description(other.description).primitive(other.primitive)
                .attributes(new LinkedList<>(other.attributes)).parent(other.parent)
                .x(other.x).y(other.y).width(other.width).height(other.height);
        require(expireDateTime.after(effectDateTime), "Expire date time must be after effect date time");
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
    private static int validateGreaterThanOrEqualZero(int value, String message) {
        require(value >= 0, message);
        return value;
    }
    private static int validateGreaterThanZero(int value, String message) {
        require(value > 0, message);
        return value;
    }
}