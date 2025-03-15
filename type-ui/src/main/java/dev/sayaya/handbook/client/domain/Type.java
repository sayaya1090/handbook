package dev.sayaya.handbook.client.domain;

import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder(toBuilder = true)
public record Type (
        String id,
        String version,
        Date effectDateTime,
        Date expireDateTime,
        String description,
        boolean primitive,
        List<Attribute> attributes,
        String parent
) {
    public Type {
        requireNonNullOrEmpty(id, "id");
        requireNonNullOrEmpty(version, "version");
        require(expireDateTime.after(effectDateTime), "Expire date time must be after effect date time");
        requireNonNull(attributes, "attributes");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    private static <T> void requireNonNull(T obj, String message) {
        require(obj != null, message + " must not be null");
    }
    private static void requireNonNullOrEmpty(String obj, String message) {
        require(obj != null && !obj.isEmpty(), message + " must not be null");
    }
}