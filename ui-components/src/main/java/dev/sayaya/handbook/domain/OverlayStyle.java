package dev.sayaya.handbook.domain;

/**
 * 오버레이 안내 스타일 열거형.
 *
 * <p><b>책임:</b> COACHMARK, SPOTLIGHT, PULSE, ARROW, BADGE 5가지 오버레이 렌더링 스타일을 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 열거형, OverlayContainer가 사용)</li></ul></p>
 */
public enum OverlayStyle {
    /** 반투명 배경 + 말풍선 팝업 */
    COACHMARK,
    /** 대상만 밝게, 나머지 어둡게 */
    SPOTLIGHT,
    /** 대상 요소 테두리 반복 강조 */
    PULSE,
    /** 대상을 가리키는 화살표 + 메시지 */
    ARROW,
    /** 대상 모서리에 알림 뱃지 */
    BADGE
}
