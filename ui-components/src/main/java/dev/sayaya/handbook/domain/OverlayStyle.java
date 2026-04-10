package dev.sayaya.handbook.domain;

/** 오버레이 안내 스타일 */
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
