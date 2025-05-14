package dev.sayaya.handbook.client.usecase.arrow;

import org.jetbrains.annotations.NotNull;

public record Point(int x, int y) {
    @NotNull
    public String toString() { return "("+x+","+y+")"; }
}
