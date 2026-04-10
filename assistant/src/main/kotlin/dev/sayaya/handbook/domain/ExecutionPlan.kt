package dev.sayaya.handbook.domain

/**
 * LLM이 생성한 실행 계획.
 *
 * @param intent 파싱된 사용자 의도 요약
 * @param steps 순서대로 실행할 단계 목록
 * @param confidence LLM의 의도 파싱 신뢰도 (0.0 ~ 1.0)
 */
data class ExecutionPlan(
    val intent: String,
    val steps: List<ExecutionStep>,
    val confidence: Double,
)
