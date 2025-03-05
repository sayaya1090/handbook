package dev.sayaya.handbook.client.domain;

// 도메인 객체
public record Box (
        String name,
        String description,
        int x, int y, int width, int height
) {
    public Box {
        if(name == null) throw new IllegalArgumentException("Name must not be null.");
        if (x <= 0) throw new IllegalArgumentException("X must be greater than 0.");
        if (y <= 0) throw new IllegalArgumentException("Y must be greater than 0.");
        if (width <= 0) throw new IllegalArgumentException("Width must be greater than 0.");
        if (height <= 0) throw new IllegalArgumentException("Height must be greater than 0.");
    }
}
