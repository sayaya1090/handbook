package dev.sayaya.handbook.client.domain;

/**
 * 에이전트 세션의 현재 상태를 나타내는 열거형.
 *
 * <p><b>책임:</b> 세션 생명주기(대기 → 계획 → 실행 → 확인 대기 → 완료/중단)를 상태 값으로 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 열거형)</li></ul></p>
 */
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
