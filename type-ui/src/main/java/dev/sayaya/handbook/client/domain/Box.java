package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/*
 Type은 현재 저장된 불변 데이터이다.
 Box는 현재 저장될 다음 Type의 데이터를 관리하는 가변 데이터이다. Type이 null이면 신규 생성 타입이다.
 */
@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public final class Box {
    private Type type;
    private String id;
    private String name;
    private String description;
    private int x;
    private int y;
    private int width;
    private int height;
    @Singular("addValue")
    private List<Value> values;

    private Box(Type type, String id, String name, String description, int x, int y, int width, int height, List<Value> values) {
        this.id         = id!=null? id : (type!=null? type.id() : generateUniqueString());
        this.type       = type;
        this.x          = validateGreaterThanZero(x, "X must be greater than 0");
        this.y          = validateGreaterThanZero(y, "Y must be greater than 0");
        this.width      = validateGreaterThanZero(width, "Width must be greater than 0");
        this.height     = validateGreaterThanZero(height, "Height must be greater than 0");
        this.name       = name!=null? name: (type!=null? type.id() : "");
        this.description= description!=null? description : (type!=null? type.description() : "");
        this.values     = values!=null ? values: new LinkedList<>();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    private static int validateGreaterThanZero(int value, String message) {
        require(value > 0, message);
        return value;
    }
    @Override public boolean equals(Object other) {
        return this == other || (other instanceof Box box && id.equals(box.id));
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
