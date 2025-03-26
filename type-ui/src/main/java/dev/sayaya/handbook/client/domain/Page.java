package dev.sayaya.handbook.client.domain;

import lombok.Builder;

@Builder(toBuilder = true)
public record Page<T>(
        T[] content,
        long totalPages,
        long totalElements
) {
    public Page {
        require(totalPages >= 0, "totalPages must be non-negative. Given: " + totalPages);
        require(totalElements >= 0, "totalElements must be non-negative. Given: " + totalElements);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
