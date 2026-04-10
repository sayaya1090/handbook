package dev.sayaya.handbook.client.domain;

/** 에이전트 세션의 현재 상태 */
public enum AgentSessionState {
    /** 세션 없음 (대기) */
    IDLE,
    /** 실행 계획 수립 중 */
    PLANNING,
    /** 커맨드 스트림 실행 중 */
    EXECUTING,
    /** 사용자 확인 대기 중 (await_confirm 수신) */
    AWAITING_CONFIRM,
    /** 작업 완료 */
    COMPLETED,
    /** 사용자가 중단함 */
    ABORTED
}
