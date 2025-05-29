package dev.sayaya.handbook.client.domain;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/*
 * Type 클래스는 타입의 정보를 나타내며, 타입의 속성, 위치, 크기 등을 포함한다.
 * 값을 바꾸려면 새로운 인스턴스를 생성해야 한다.
 * 원본값이
 */
@Getter
@ToString(exclude = {"original"})
@EqualsAndHashCode(exclude = {"original"}) // original 필드를 equals/hashCode에서 제외
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Type {
    private final String id;
    private final String name;
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
    private Type original; // 원본 타입, 변경 전 상태를 저장하기 위해 사용
    @Builder.Default
    private TypeState state = TypeState.NOT_CHANGE;

    private Type(String id, String name, String version, Date effectDateTime, Date expireDateTime, String description, boolean primitive, List<Attribute> attributes, String parent, int x, int y, int width, int height, Type original, TypeState state) {
        this.id = validateNonNullOrEmpty(id, "id");
        this.name = name;
        this.version = version;
        this.effectDateTime = validateNonNull(effectDateTime, "Effect date time must not be null");
        this.expireDateTime = validateNonNull(expireDateTime, "Expire date time must not be null");
        this.description = description;
        this.primitive = primitive;
        this.attributes = validateNonNull(attributes, "attributes");
        this.parent = parent;
        this.x = validateGreaterThanOrEqualZero(x, "X must be greater than or equal 0");
        this.y = validateGreaterThanOrEqualZero(y, "Y must be greater than or equal 0");
        this.width = validateGreaterThanZero(width, "Width must be greater than 0");
        this.height = validateGreaterThanZero(height, "Height must be greater than 0");
        this.original = original!= null ? original : this;
        if(state == TypeState.DELETE) this.state = state;
        else this.state = this.original.equals(this) ? TypeState.NOT_CHANGE : TypeState.CHANGE;
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
    public enum TypeState {
        NOT_CHANGE, CHANGE, DELETE
    }
}