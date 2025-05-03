package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

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
    private int x;
    private int y;
    private int width;
    private int height;
    @Singular("addValue")
    private List<Value> values;

    private Box(Type type, int x, int y, int width, int height, List<Value> values) {
        this.type       = type;
        this.x          = validateGreaterThanOrEqualZero(x, "X must be greater than or equal 0");
        this.y          = validateGreaterThanOrEqualZero(y, "Y must be greater than or equal 0");
        this.width      = validateGreaterThanZero(width, "Width must be greater than 0");
        this.height     = validateGreaterThanZero(height, "Height must be greater than 0");
        this.values     = values!=null ? values: new LinkedList<>();
    }
    public String id() {
        return type.id() + "$$$" + type.version();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    private static int validateGreaterThanOrEqualZero(int value, String message) {
        require(value >= 0, message);
        return value;
    }
    private static int validateGreaterThanZero(int value, String message) {
        require(value > 0, message);
        return value;
    }
    @Override public boolean equals(Object other) {
        return this == other || (other instanceof Box box && id().equals(box.id()));
    }
    @Override public int hashCode() {
        return id().hashCode();
    }
}
