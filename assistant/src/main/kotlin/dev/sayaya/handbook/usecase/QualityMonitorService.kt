package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.QualityIssue
import reactor.core.publisher.Mono
import java.util.*

/**
 * 품질 모니터링 서비스.
 *
 * **책임:** 전체 워크스페이스 스캔(execute) 및 특정 타입/문서 대상 검증(validate)을 수행하고,
 * 발견된 이슈를 AGENT_COMMAND NOTIFY 이벤트로 발행한다.
 *
 * **의존관계:**
 * - [QualityMonitor] — 실제 검증 로직 (필수 필드 누락, 중복 시리얼, 수치 이상치)
 * - [AgentCommandEventPublisher] — 검증 결과 Kafka 이벤트 발행
 */
class QualityMonitorService(
    private val monitor: QualityMonitor,
    private val eventPublisher: AgentCommandEventPublisher,
) {
    fun execute(workspace: UUID): Mono<Void> {
        return monitor.scan(workspace)
            .doOnNext { issue -> publishIssue(workspace, issue) }
            .then()
    }

    /**
     * VALIDATION_REQUESTED 이벤트에 의해 트리거되는 대상 검증.
     * 전체 스캔 후 typeId/documentId로 필터링하여 해당 이슈만 발행한다.
     */
    fun validate(workspace: UUID, typeId: String, typeVersion: String?, documentId: String?): Mono<Void> {
        return monitor.scan(workspace)
            .filter { issue -> issue.type == typeId }
            .filter { issue -> documentId == null || issue.serial == documentId }
            .doOnNext { issue -> publishIssue(workspace, issue) }
            .then()
    }

    private fun publishIssue(workspace: UUID, issue: QualityIssue) {
        val severity = when (issue.severity) {
            QualityIssue.Severity.ERROR -> "error"
            QualityIssue.Severity.WARNING -> "warning"
            else -> "info"
        }
        eventPublisher.publish(workspace, 0, AgentCommand(
            type = CommandType.NOTIFY,
            target = issue.message,
            payload = mapOf("level" to severity, "type" to issue.type, "serial" to issue.serial),
        ))
    }
}
