package dev.sayaya.handbook.domain

/**
 * 에이전트 → 프론트엔드 UI 제어 커맨드.
 *
 * @param type 커맨드 유형
 * @param target 대상 CSS 선택자 또는 식별자 (nullable)
 * @param payload 커맨드별 추가 데이터 (nullable)
 */
data class AgentCommand(
    val type: CommandType,
    val target: String? = null,
    val payload: Map<String, Any>? = null,
)
