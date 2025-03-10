package dev.sayaya.handbook.client.domain;

import lombok.Data;
import lombok.experimental.Accessors;

// 도메인 객체
@Data
@Accessors(fluent = true)
public final class Box {
    private String name;
    private String description;
    private int x;
    private int y;
    private int width;
    private int height;

    // 생성자
    public Box(String name, String description, int x, int y, int width, int height) {
        if (name == null) throw new IllegalArgumentException("Name must not be null.");
        if (x <= 0) throw new IllegalArgumentException("X must be greater than 0.");
        if (y <= 0) throw new IllegalArgumentException("Y must be greater than 0.");
        if (width <= 0) throw new IllegalArgumentException("Width must be greater than 0.");
        if (height <= 0) throw new IllegalArgumentException("Height must be greater than 0.");
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
