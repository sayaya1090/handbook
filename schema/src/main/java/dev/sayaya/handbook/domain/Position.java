package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class Position implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("x") @JsProperty private int x;
    @JsonProperty("y") @JsProperty private int y;
    @JsonProperty("width") @JsProperty private int width;
    @JsonProperty("height") @JsProperty private int height;

    @JsOverlay @JsIgnore
    public static Position of(int x, int y, int width, int height) {
        Position pos = new Position();
        pos.x(x);
        pos.y(y);
        pos.width(width);
        pos.height(height);
        return pos;
    }

    @JsOverlay @JsIgnore
    public Position move(int dx, int dy) {
        return Position.of(x() + dx, y() + dy, width(), height());
    }
}
