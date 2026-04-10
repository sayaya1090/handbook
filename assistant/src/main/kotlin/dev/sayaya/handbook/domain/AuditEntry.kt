package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.UUID

data class AuditEntry(
    val id: UUID = UUID.randomUUID(),
    val workspace: UUID,
    val timestamp: Instant = Instant.now(),
    val userMessage: String,
    val intent: String,
    val confidence: Double,
    val plan: ExecutionPlan,
    val status: Status,
) {
    enum class Status { REQUESTED, CONFIRMED, EXECUTING, COMPLETED, ABORTED }
}
