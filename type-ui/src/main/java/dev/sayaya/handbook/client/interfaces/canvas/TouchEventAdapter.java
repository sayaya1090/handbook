package dev.sayaya.handbook.client.interfaces.canvas;

import elemental2.dom.*;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 터치 이벤트를 마우스 이벤트로 변환하는 어댑터.
 *
 * <p><b>책임:</b> touchstart/touchmove/touchend 이벤트를 mousedown/mousemove/mouseup으로 변환하여
 * 캔버스의 드래그/드롭/리사이즈를 모바일에서도 동작하게 한다.
 * 500ms 롱프레스 시 contextmenu 이벤트를 트리거한다.</p>
 * <p><b>의존관계:</b> 없음 (DOM API만 사용하는 독립 어댑터)</p>
 * <p><b>주의:</b> 핀치 줌(touches.length != 1)은 무시한다.
 * 이동 거리가 10px 이상이면 롱프레스가 취소된다.
 * touchmove 시 preventDefault()로 스크롤을 방지한다.</p>
 */
@Singleton
public class TouchEventAdapter {
    private static final int LONGPRESS_MS = 500;
    private double longpressTimer = -1;
    private boolean longpressFired = false;
    private double startX, startY;

    @Inject TouchEventAdapter() {}

    /** 대상 요소에 터치 → 마우스 이벤트 변환을 바인딩한다. */
    public void bind(HTMLElement element) {
        element.addEventListener("touchstart", this::onTouchStart);
        element.addEventListener("touchmove", this::onTouchMove);
        element.addEventListener("touchend", this::onTouchEnd);
        element.addEventListener("touchcancel", this::onTouchEnd);
    }

    private void onTouchStart(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        if (e.touches.length != 1) return; // 핀치 줌은 별도 처리
        Touch touch = e.touches.item(0);
        startX = touch.clientX;
        startY = touch.clientY;
        longpressFired = false;

        // 롱프레스 타이머 시작
        longpressTimer = DomGlobal.setTimeout(args -> {
            longpressFired = true;
            dispatchMouseEvent(e.target, "contextmenu", touch);
        }, LONGPRESS_MS);

        dispatchMouseEvent(e.target, "mousedown", touch);
    }

    private void onTouchMove(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        if (e.touches.length != 1) return;
        Touch touch = e.touches.item(0);

        // 이동 거리가 5px 이상이면 롱프레스 취소
        double dx = touch.clientX - startX;
        double dy = touch.clientY - startY;
        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
            cancelLongpress();
        }

        dispatchMouseEvent(e.target, "mousemove", touch);
        evt.preventDefault(); // 스크롤 방지
    }

    private void onTouchEnd(Event evt) {
        TouchEvent e = (TouchEvent) evt;
        cancelLongpress();
        // 2026-05-06: 롱프레스 발화 여부와 관계없이 터치가 종료되면 무조건 mouseup 발행.
        // 이를 통해 모바일에서 드래그 중 롱프레스가 떠도 손가락을 떼면 드롭 액션이 정상 실행됨.
        if (e.changedTouches.length > 0) {
            Touch touch = e.changedTouches.item(0);
            dispatchMouseEvent(e.target, "mouseup", touch);
        }
    }

    private void cancelLongpress() {
        if (longpressTimer >= 0) {
            DomGlobal.clearTimeout(longpressTimer);
            longpressTimer = -1;
        }
    }

    private static void dispatchMouseEvent(EventTarget target, String type, Touch touch) {
        MouseEventInit init = MouseEventInit.create();
        init.setBubbles(true);
        init.setCancelable(true);
        init.setClientX(touch.clientX);
        init.setClientY(touch.clientY);
        init.setButton((short) ("contextmenu".equals(type) ? 2 : 0));
        target.dispatchEvent(new MouseEvent(type, init));
    }
}
