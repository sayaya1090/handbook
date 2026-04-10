package dev.sayaya.handbook.domain

/**
 * 실행 계획 내 단일 단계.
 *
 * <p><b>책임:</b> 실행 계획의 개별 커맨드를 그룹 및 순서 정보와 함께 보유한다.</p>
 *
 * <p><b>주의:</b> 같은 group 번호를 가진 단계들은 병렬로 실행된다.
 * group 필드의 기본값은 order와 동일하여 기존 순차 실행과 호환된다.</p>
 *
 * @param group 병렬 실행 그룹 번호. 같은 그룹의 단계들은 동시에 실행된다.
 * @param order 실행 순서 (0-based)
 * @param command 실행할 에이전트 커맨드
 * @param description 사용자에게 표시할 단계 설명
 */
data class ExecutionStep(
    val group: Int = 0,
    val order: Int,
    val command: AgentCommand,
    val description: String,
)
