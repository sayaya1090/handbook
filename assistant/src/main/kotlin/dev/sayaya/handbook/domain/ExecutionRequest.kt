package dev.sayaya.handbook.domain

import java.util.*

/**
 * 실행 요청 응답 래퍼.
 *
 * <p><b>책임:</b> request() 호출 결과로 실행 ID와 실행 계획을 함께 반환하여
 * 클라이언트가 후속 execute/respond/abort 호출에 executionId를 사용할 수 있게 한다.</p>
 *
 * @param executionId 실행 고유 식별자 (auditId와 동일)
 * @param plan 파싱된 실행 계획
 */
data class ExecutionRequest(
    val executionId: UUID,
    val plan: ExecutionPlan,
)
