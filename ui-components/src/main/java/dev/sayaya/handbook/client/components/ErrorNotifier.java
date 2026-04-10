package dev.sayaya.handbook.client.components;

import dev.sayaya.handbook.domain.ToastLevel;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

/**
 * 글로벌 에러 알림 유틸리티. window CustomEvent 기반.
 *
 * <p><b>역할:</b> API 클래스 등에서 발생한 오류를 사용자에게 토스트로 표시하기 위한
 * 전역 이벤트 발행/구독 브릿지.</p>
 *
 * <p><b>책임:</b> {@code handbook-error} CustomEvent를 발행하고, 구독 측(ToastContainer)이
 * 이벤트를 수신하여 에러 토스트를 렌더링한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ToastContainer} — 구독 측에서 토스트 표시</li>
 *   <li>{@link ToastLevel} — 기본 ERROR 레벨 사용</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 정적 메서드만 제공하며, 인스턴스를 생성하지 않는다.
 * {@link #install(ToastContainer)}를 모듈 초기화 시 한 번 호출해야 한다.</p>
 */
public final class ErrorNotifier {
    private static final String EVENT_NAME = "handbook-error";
    private static boolean listenerInstalled = false;

    private ErrorNotifier() {}

    /**
     * 에러 메시지를 글로벌 CustomEvent로 발행한다.
     * 설치된 ToastContainer가 있으면 토스트로 표시되고,
     * 없으면 콘솔에만 기록된다.
     */
    public static void notify(String message) {
        DomGlobal.console.error("[handbook-error] " + message);
        @SuppressWarnings("unchecked")
        CustomEventInit<String> init = Js.cast(CustomEventInit.create());
        init.setDetail(message);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(EVENT_NAME, init));
    }

    /**
     * ToastContainer를 글로벌 에러 이벤트 리스너에 연결한다.
     * 모듈 초기화 시 한 번만 호출하면 된다. 중복 호출은 무시한다.
     */
    public static void install(ToastContainer toastContainer) {
        if (listenerInstalled) return;
        listenerInstalled = true;
        DomGlobal.window.addEventListener(EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail != null) {
                toastContainer.show(ToastLevel.ERROR, String.valueOf(detail));
            }
        });
    }
}
