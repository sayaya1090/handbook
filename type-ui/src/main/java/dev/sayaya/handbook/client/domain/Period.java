package dev.sayaya.handbook.client.domain;

import lombok.Builder;

import java.util.Date;

@Builder(toBuilder = true)
public record Period (
        Date effectDateTime,
        Date expireDateTime
) {
    public Period {
        require(expireDateTime.after(effectDateTime), "Expire date time must be after effect date time");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
