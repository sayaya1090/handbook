package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

// 도메인 객체
@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public final class Box {
    private String id;
    private String name;
    private String description;
    private int x;
    private int y;
    private int width;
    private int height;
    private LinkedList<Value> values;

    private Box(String id, String name, String description, int x, int y, int width, int height, LinkedList<Value> values) {
        this.id         = id != null ? id : generateUniqueString();
        this.name       = validateNotNullOrEmpty(name, "Name must not be null or empty");
        this.description= description;
        this.x          = validateGreaterThanZero(x, "X must be greater than 0");
        this.y          = validateGreaterThanZero(y, "Y must be greater than 0");
        this.width      = validateGreaterThanZero(width, "Width must be greater than 0");
        this.height     = validateGreaterThanZero(height, "Height must be greater than 0");
        this.values     = values;
    }
    private static String validateNotNullOrEmpty(String value, String message) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException(message);
        return value;
    }
    private static int validateGreaterThanZero(int value, String message) {
        if (value <= 0) throw new IllegalArgumentException(message);
        return value;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if(!(other instanceof Box box)) return false;
        return id.equals(box.id);
    }
    @Override public int hashCode() {
        return id.hashCode();
    }
    private static String generateUniqueString() {
        long timestamp = new Date().getTime(); // 현재 시간 (밀리초)
        String randomPortion = Double.toString(Math.random()).substring(2, 8); // 랜덤 숫자 (문자열)
        return Long.toString(timestamp, 36) + randomPortion; // 시간+랜덤 조합 문자열
    }
}
