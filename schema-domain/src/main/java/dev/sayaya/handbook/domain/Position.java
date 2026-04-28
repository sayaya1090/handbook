package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 캔버스 위 타입 박스의 위치와 크기를 표현하는 값 객체(GWT JsInterop native).
 *
 * <p><b>책임:</b> 백엔드 TypeLayout.Position에 대응하는 x, y, width, height 값을 보유하며,
 * 이동(move)과 리사이즈(resize) 시 새 인스턴스를 반환하는 불변 연산을 제공한다.</p>
 * <p><b>의존관계:</b> 없음 (자립 값 객체)</p>
 * <p><b>주의:</b> 좌표 단위는 픽셀(px). 캔버스 좌상단 기준 절대 좌표.</p>
 */
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
