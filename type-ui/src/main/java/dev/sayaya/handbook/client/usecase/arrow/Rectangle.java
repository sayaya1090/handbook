package dev.sayaya.handbook.client.usecase.arrow;

import org.jetbrains.annotations.NotNull;

public record Rectangle(int x, int y, int w, int h) {
    public Point leftMid() {
        return new Point(x, y + h / 2);
    }
    public Point rightMid() {
        return new Point(x + w, y + h / 2);
    }
    public Point topMid() {
        return new Point(x + w / 2, y);
    }
    public Point bottomMid() {
        return new Point(x + w / 2, y + h);
    }
    @NotNull public String toString() { return "("+x+","+y+","+w+","+h+")"; }
}
