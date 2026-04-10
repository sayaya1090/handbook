package dev.sayaya.handbook.client.usecase.arrow;

/** 사각형 영역. */
public record Rectangle(int x, int y, int width, int height) {
    public Point center() { return new Point(x + width / 2, y + height / 2); }
    public int right() { return x + width; }
    public int bottom() { return y + height; }
}
