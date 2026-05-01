package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

/**
 * AI 어시스턴트 실행의 감사 기록 엔트리.
 *
 * <p><b>책임:</b> 사용자 요청부터 실행 완료까지의 전체 라이프사이클을 기록한다.
 * 실행 완료 시 생성된 Artifact를 보관한다.</p>
 *
 * <p><b>주의:</b> artifact 필드는 COMPLETED 상태에서만 값이 설정된다.</p>
 */
data class AuditEntry(
    val id: UUID = UUID.randomUUID(),
    val workspace: UUID,
    val timestamp: Instant = Instant.now(),
    val userMessage: String,
    val intent: String,
    val confidence: Double,
    val plan: ExecutionPlan,
    val status: Status,
    val artifact: Artifact? = null,
) {
    enum class Status { REQUESTED, CONFIRMED, EXECUTING, COMPLETED, ABORTED }
}
