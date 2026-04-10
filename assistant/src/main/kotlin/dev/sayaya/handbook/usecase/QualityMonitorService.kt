package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.QualityIssue
import reactor.core.publisher.Mono
import java.util.UUID

class QualityMonitorService(
    private val monitor: QualityMonitor,
    private val eventPublisher: AgentCommandEventPublisher,
) {
    fun execute(workspace: UUID): Mono<Void> {
        return monitor.scan(workspace)
            .doOnNext { issue ->
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
            .then()
    }
}
