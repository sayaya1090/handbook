package dev.sayaya.handbook.client.interfaces.canvas;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.Touch;
import elemental2.dom.TouchEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 캔버스에 두 손가락 핀치 줌을 제공하는 핸들러.
 *
 * <p><b>책임:</b> touchstart에서 두 손가락이 감지되면 초기 거리를 기록하고,
 * touchmove에서 거리 변화에 따라 캔버스의 CSS transform scale을 조절한다.
 * touchend에서 핀치 상태를 해제한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (DOM API만 사용하는 독립 핸들러)</p>
 *
 * <p><b>주의:</b> 줌 범위는 0.5x ~ 3.0x로 제한된다.
 * transform-origin은 두 손가락 중앙점 기준이다.
 * 단일 터치(touches.length == 1)는 무시한다.</p>
 */
@Singleton
public class PinchZoomHandler {
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 3.0;

    private double initialDistance = 0;
    private double currentScale = 1.0;
    private double baseScale = 1.0;
    private boolean pinching = false;

    @Inject PinchZoomHandler() {}

    /** 대상 요소에 핀치 줌 이벤트를 바인딩한다. */
    public void bind(HTMLElement element) {
        element.addEventListener("touchstart", this::onTouchStart);
        element.addEventListener("touchmove", this::onTouchMove);
        element.addEventListener("touchend", this::onTouchEnd);
        element.addEventListener("touchcancel", this::onTouchEnd);
    }

    private void onTouchStart(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        if (e.touches.length == 2) {
            pinching = true;
            initialDistance = distance(e.touches.item(0), e.touches.item(1));
            baseScale = currentScale;
            evt.preventDefault();
        }
    }

    private void onTouchMove(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        if (!pinching || e.touches.length != 2) return;
        evt.preventDefault();

        double dist = distance(e.touches.item(0), e.touches.item(1));
        double scale = baseScale * (dist / initialDistance);
        currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

        Touch t0 = e.touches.item(0);
        Touch t1 = e.touches.item(1);
        double cx = (t0.clientX + t1.clientX) / 2;
        double cy = (t0.clientY + t1.clientY) / 2;

        HTMLElement target = (HTMLElement) e.currentTarget;
        target.style.setProperty("transform-origin", cx + "px " + cy + "px");
        target.style.setProperty("transform", "scale(" + currentScale + ")");
    }

    private void onTouchEnd(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        if (e.touches.length < 2) {
            pinching = false;
        }
    }

    private static double distance(Touch a, Touch b) {
        double dx = a.clientX - b.clientX;
        double dy = a.clientY - b.clientY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** 현재 줌 스케일을 반환한다. */
    public double getScale() { return currentScale; }

    /** 줌 스케일을 1.0으로 리셋한다. */
    public void reset(HTMLElement element) {
        currentScale = 1.0;
        baseScale = 1.0;
        element.style.setProperty("transform", "scale(1)");
    }
}
