package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** 캔버스 위 타입 박스의 위치와 크기. backend TypeLayout.Position과 대응. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Position {
    public int x;
    public int y;
    public int width;
    public int height;

    @JsOverlay
    public static Position of(int x, int y, int width, int height) {
        Position p = new Position();
        p.x = x;
        p.y = y;
        p.width = width;
        p.height = height;
        return p;
    }

    @JsOverlay
    public final Position move(int dx, int dy) {
        return of(x + dx, y + dy, width, height);
    }

    @JsOverlay
    public final Position resize(int newWidth, int newHeight) {
        return of(x, y, newWidth, newHeight);
    }
}
