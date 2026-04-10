package dev.sayaya.handbook.domain;

/**
 * 토스트 메시지의 심각도 레벨 열거형.
 *
 * <p><b>책임:</b> INFO, SUCCESS, WARNING, ERROR 4단계 심각도를 정의하여 ToastContainer의 스타일과 자동 닫힘 동작을 결정한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 열거형)</li></ul></p>
 */
public enum ToastLevel {
    INFO, SUCCESS, WARNING, ERROR
}
