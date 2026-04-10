package dev.sayaya.handbook.domain

/**
 * 실행 계획 내 단일 단계.
 *
 * @param order 실행 순서 (0-based)
 * @param command 실행할 에이전트 커맨드
 * @param description 사용자에게 표시할 단계 설명
 */
data class ExecutionStep(
    val order: Int,
    val command: AgentCommand,
    val description: String,
)
