package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Position {
    @JsonProperty("x") @JsProperty public int x;
    @JsonProperty("y") @JsProperty public int y;
    @JsonProperty("width") @JsProperty public int width;
    @JsonProperty("height") @JsProperty public int height;

    @JsOverlay @JsIgnore
    public static Position of(int x, int y, int width, int height) {
        Position pos = new Position();
        pos.x = x;
        pos.y = y;
        pos.width = width;
        pos.height = height;
        return pos;
    }
}
