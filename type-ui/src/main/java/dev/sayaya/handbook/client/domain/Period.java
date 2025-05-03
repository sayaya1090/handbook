package dev.sayaya.handbook.client.domain;

import lombok.Builder;

import java.util.Date;

@Builder(toBuilder = true)
public record Period (
        String workspace,
        Date effectDateTime,
        Date expireDateTime
) {
    public Period {
        requireNonNullOrEmpty(workspace, "workspace");
        require(expireDateTime.after(effectDateTime), "Expire date time must be after effect date time");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    private static void requireNonNullOrEmpty(String obj, String message) {
        require(obj != null && !obj.isEmpty(), message + " must not be null nor empty");
    }
}
